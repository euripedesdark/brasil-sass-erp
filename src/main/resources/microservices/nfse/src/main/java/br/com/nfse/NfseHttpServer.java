package br.com.nfse;

import br.com.nfse.dto.AutorEvento;
import br.com.nfse.dto.EventoResult;
import br.com.nfse.dto.NFSeResult;
import br.com.nfse.dto.enuns.AmbienteEnum;
import br.com.nfse.utils.DateUtils;
import br.com.nfse.utils.StringUtils;
import br.com.nfse.xsd.TCDPS;
import br.com.nfse.xsd.TCInfoDPS;
import br.com.nfse.xsd.TCInfoPrestador;
import br.com.nfse.xsd.TCInfoPessoa;
import br.com.nfse.xsd.TCEndereco;
import br.com.nfse.xsd.TCEnderNac;
import br.com.nfse.xsd.TCRegTrib;
import br.com.nfse.xsd.TCServ;
import br.com.nfse.xsd.TCLocPrest;
import br.com.nfse.xsd.TCCServ;
import br.com.nfse.xsd.TCInfoValores;
import br.com.nfse.xsd.TCVServPrest;
import br.com.nfse.xsd.TCInfoTributacao;
import br.com.nfse.xsd.TCTribMunicipal;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;

/**
 * Adapter HTTP minimo para o cliente NFS-e Nacional que ja existe neste
 * repositorio.
 *
 * <p>O ponto importante e arquitetural: o cliente nacional continua isolado
 * no seu classpath Java 8/JAXB/OkHttp e nao entra no Spring Boot 3 do ERP.
 * Este processo somente transforma o contrato JSON do ERP em DPS 1.01 e
 * devolve o resultado do SEFIN Nacional.
 */
public final class NfseHttpServer {

    private static final Gson JSON = new GsonBuilder().serializeNulls().create();

    private NfseHttpServer() {
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getProperty("nfse.http.port",
                env("NFSE_HTTP_PORT", "4580")));
        HttpServer server = HttpServer.create(new InetSocketAddress(
                System.getProperty("nfse.http.bind", env("NFSE_HTTP_BIND", "127.0.0.1")),
                port), 0);

        server.createContext("/status", NfseHttpServer::status);
        server.createContext("/api/nfse-nacional/emitir-rps", NfseHttpServer::emitir);
        server.createContext("/api/nfse-nacional/consultar", NfseHttpServer::consultar);
        server.createContext("/api/nfse-nacional/cancelar", NfseHttpServer::cancelar);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        System.out.println("NFS-e Nacional adapter listening on " + port);
    }

    private static void status(HttpExchange x) throws IOException {
        responder(x, 200, Map.of(
                "sucesso", true,
                "provedor", "SEFIN_NACIONAL",
                "ambiente", ambiente().name()));
    }

    private static void emitir(HttpExchange x) throws IOException {
        try {
            Emissao r = JSON.fromJson(corpo(x), Emissao.class);
            validar(r);

            ConfigManager config = config();
            TCDPS dps = montarDps(r, config);

            NFSeResult result = Nfse.builder()
                    .config(config)
                    .assinar(true)
                    .validar(true)
                    .enviarDps(dps);

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("sucesso", result.isSuccessful());
            out.put("numero_nfse", result.getChaveAcesso() == null ? "" : extrairNumero(result.getNfseXml()));
            out.put("codigo_verificacao", result.getChaveAcesso() == null ? "" : result.getChaveAcesso());
            out.put("chave_nota_nacional", result.getChaveAcesso());
            out.put("id_dps", result.getIdDPS());
            out.put("xml", safeXml(result));
            out.put("mensagem", result.getErrosMessage());
            responder(x, result.isSuccessful() ? 200 : 422, out);
        } catch (Exception e) {
            responder(x, 422, Map.of("sucesso", false, "mensagem", mensagem(e)));
        }
    }

    private static void consultar(HttpExchange x) throws IOException {
        try {
            String chave = x.getRequestURI().getPath()
                    .substring("/api/nfse-nacional/consultar/".length());
            if (chave.isBlank()) {
                responder(x, 400, Map.of("sucesso", false, "mensagem", "Chave NFS-e obrigatoria."));
                return;
            }

            NFSeResult result = Nfse.builder()
                    .config(config())
                    .chNFSe(chave)
                    .consultaXml();

            responder(x, result.isSuccessful() ? 200 : 404, Map.of(
                    "sucesso", result.isSuccessful(),
                    "chave_nota_nacional", chave,
                    "xml", safeXml(result),
                    "mensagem", result.getErrosMessage()));
        } catch (Exception e) {
            responder(x, 404, Map.of("sucesso", false, "mensagem", mensagem(e)));
        }
    }

    private static void cancelar(HttpExchange x) throws IOException {
        try {
            Cancelamento r = JSON.fromJson(corpo(x), Cancelamento.class);
            if (r == null || vazio(r.chaveNfse) || vazio(r.cnpjAutor) || vazio(r.motivo)) {
                responder(x, 400, Map.of("sucesso", false,
                        "mensagem", "chaveNfse, cnpjAutor e motivo sao obrigatorios."));
                return;
            }

            EventoResult result = Nfse.builder()
                    .config(config())
                    .chNFSe(r.chaveNfse)
                    .autor(AutorEvento.cnpj(digitos(r.cnpjAutor)))
                    .dhEvento(ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")))
                    .cancelar("1", r.motivo);

            responder(x, result.isSuccessful() ? 200 : 422, Map.of(
                    "sucesso", result.isSuccessful(),
                    "chave_nota_nacional", r.chaveNfse,
                    "xml", safeXmlEvento(result),
                    "mensagem", result.getErrosMessage()));
        } catch (Exception e) {
            responder(x, 422, Map.of("sucesso", false, "mensagem", mensagem(e)));
        }
    }

    private static TCDPS montarDps(Emissao r, ConfigManager config) {
        TCDPS dps = new TCDPS();
        dps.setVersao("1.01");

        TCInfoDPS inf = new TCInfoDPS();
        inf.setTpAmb(config.getAmbiente().getValue());
        inf.setDhEmi(DateUtils.formatWithZone(ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))));
        inf.setVerAplic("BrasilCloudERP");
        inf.setSerie(vazio(r.serieRps) ? "1" : r.serieRps);
        inf.setNDPS(vazio(r.numeroRps) ? "1" : r.numeroRps);
        inf.setDCompet(vazio(r.dataCompetencia)
                ? java.time.LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString()
                : r.dataCompetencia);
        inf.setTpEmit("1");
        inf.setCLocEmi(r.codigoIbge);

        inf.setPrest(prestador(r));
        inf.setToma(tomador(r));
        inf.setServ(servico(r));
        inf.setValores(valores(r));

        String doc = digitos(r.cnpjPrestador);
        String id = r.codigoIbge
                + "2"
                + StringUtils.zerosParaEsquerda(doc, 14)
                + StringUtils.zerosParaEsquerda(inf.getSerie(), 5)
                + StringUtils.zerosParaEsquerda(inf.getNDPS(), 15);
        inf.setId("DPS" + id);
        dps.setInfDPS(inf);
        return dps;
    }

    private static TCInfoPrestador prestador(Emissao r) {
        TCInfoPrestador p = new TCInfoPrestador();
        p.setCNPJ(digitos(r.cnpjPrestador));
        p.setIM(r.inscricaoMunicipalPrestador);
        p.setXNome(r.nomePrestador);
        p.setEnd(endereco(r));
        p.setFone(r.telefonePrestador);
        p.setEmail(r.emailPrestador);

        TCRegTrib reg = new TCRegTrib();
        reg.setOpSimpNac(vazio(r.optanteSimplesNacional) ? "3" : r.optanteSimplesNacional);
        reg.setRegApTribSN("1");
        reg.setRegEspTrib("0");
        p.setRegTrib(reg);
        return p;
    }

    private static TCInfoPessoa tomador(Emissao r) {
        TCInfoPessoa p = new TCInfoPessoa();
        if (!vazio(r.cnpjTomador)) {
            p.setCNPJ(digitos(r.cnpjTomador));
        } else if (!vazio(r.cpfTomador)) {
            p.setCPF(digitos(r.cpfTomador));
        }
        p.setXNome(r.nomeTomador);
        p.setEnd(enderecoTomador(r));
        p.setFone(r.telefoneTomador);
        p.setEmail(r.emailTomador);
        return p;
    }

    private static TCServ servico(Emissao r) {
        TCServ s = new TCServ();

        TCLocPrest loc = new TCLocPrest();
        loc.setCLocPrestacao(vazio(r.codigoMunicipioPrestacao) ? r.codigoIbge : r.codigoMunicipioPrestacao);
        s.setLocPrest(loc);

        TCCServ c = new TCCServ();
        c.setCTribNac(r.codigoTributacaoNacional);
        c.setXDescServ(r.discriminacao);
        c.setCIntContrib(r.codigoAtividadeEconomica);
        c.setCNBS(r.codigoNbs);
        s.setCServ(c);
        return s;
    }

    private static TCInfoValores valores(Emissao r) {
        TCInfoValores v = new TCInfoValores();
        TCVServPrest serv = new TCVServPrest();
        serv.setVServ(r.valorServico);
        v.setVServPrest(serv);

        TCInfoTributacao trib = new TCInfoTributacao();
        TCTribMunicipal mun = new TCTribMunicipal();
        mun.setTribISSQN(vazio(r.issRetido) || !r.issRetido ? "1" : "2");
        mun.setPAliq(vazio(r.aliquotaIss) ? "0" : r.aliquotaIss);
        mun.setTpRetISSQN(vazio(r.issRetido) || !r.issRetido ? "1" : "2");
        trib.setTribMun(mun);
        v.setTrib(trib);
        return v;
    }

    private static TCEndereco endereco(Emissao r) {
        TCEndereco e = new TCEndereco();
        e.setXLgr(r.logradouroPrestador);
        e.setNro(r.numeroPrestador);
        e.setXCpl(r.complementoPrestador);
        e.setXBairro(r.bairroPrestador);
        TCEnderNac n = new TCEnderNac();
        n.setCMun(r.codigoIbge);
        n.setCEP(digitos(r.cepPrestador));
        e.setEndNac(n);
        return e;
    }

    private static TCEndereco enderecoTomador(Emissao r) {
        TCEndereco e = new TCEndereco();
        e.setXLgr(r.logradouroTomador);
        e.setNro(r.numeroTomador);
        e.setXCpl(r.complementoTomador);
        e.setXBairro(r.bairroTomador);
        TCEnderNac n = new TCEnderNac();
        n.setCMun(vazio(r.codigoMunicipioTomador) ? r.codigoIbge : r.codigoMunicipioTomador);
        n.setCEP(digitos(r.cepTomador));
        e.setEndNac(n);
        return e;
    }

    private static ConfigManager config() throws Exception {
        String path = env("NFSE_NACIONAL_CERT_PATH", System.getProperty("nfse.nacional.cert.path"));
        String pass = env("NFSE_NACIONAL_CERT_PASSWORD", System.getProperty("nfse.nacional.cert.password"));
        if (vazio(path) || vazio(pass)) {
            throw new IllegalStateException("NFSE_NACIONAL_CERT_PATH e NFSE_NACIONAL_CERT_PASSWORD sao obrigatorios.");
        }

        CertificateManager cert = CertificateManager.builder()
                .fromFile(new java.io.File(path))
                .password(pass)
                .zoneId(ZoneId.of("America/Sao_Paulo"))
                .build();

        return ConfigManager.builder()
                .certificado(cert)
                .ambiente(ambiente())
                .build();
    }

    private static AmbienteEnum ambiente() {
        return "PRODUCAO".equalsIgnoreCase(env("NFSE_NACIONAL_AMBIENTE", "HOMOLOGACAO"))
                ? AmbienteEnum.PRODUCAO : AmbienteEnum.HOMOLOGACAO;
    }

    private static String safeXml(NFSeResult r) {
        try {
            return r.getNfseXml();
        } catch (Exception e) {
            return r.getDpsXml();
        }
    }

    private static String safeXmlEvento(EventoResult r) {
        try {
            return r.getXmlRetorno();
        } catch (Exception e) {
            return r.getXmlEnvio();
        }
    }

    private static String extrairNumero(String xml) {
        if (xml == null || xml.isBlank()) return "";
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("<nNFSe>([^<]+)</nNFSe>")
                .matcher(xml);
        return m.find() ? m.group(1) : "";
    }

    private static void validar(Emissao r) {
        if (r == null) throw new IllegalArgumentException("Corpo da emissao obrigatorio.");
        if (digitos(r.cnpjPrestador).length() != 14) throw new IllegalArgumentException("CNPJ do prestador invalido.");
        if (digitos(r.codigoIbge).length() != 7) throw new IllegalArgumentException("Codigo IBGE do municipio invalido.");
        if (vazio(r.inscricaoMunicipalPrestador)) throw new IllegalArgumentException("Inscricao Municipal do prestador obrigatoria.");
        if (vazio(r.valorServico)) throw new IllegalArgumentException("Valor do servico obrigatorio.");
        if (vazio(r.codigoTributacaoNacional)) throw new IllegalArgumentException("Codigo de tributacao nacional obrigatorio.");
    }

    private static String corpo(HttpExchange x) throws IOException {
        return new String(x.getRequestBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
    }

    private static void responder(HttpExchange x, int status, Object body) throws IOException {
        byte[] data = JSON.toJson(body).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        x.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        x.sendResponseHeaders(status, data.length);
        try (OutputStream out = x.getResponseBody()) {
            out.write(data);
        }
    }

    private static String mensagem(Exception e) {
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }

    private static String env(String nome, String padrao) {
        String v = System.getenv(nome);
        return v == null || v.isBlank() ? (padrao == null ? "" : padrao) : v;
    }

    private static String digitos(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    private static boolean vazio(String s) {
        return s == null || s.isBlank();
    }

    public static final class Emissao {
        String cnpjPrestador;
        String inscricaoMunicipalPrestador;
        String nomePrestador;
        String codigoIbge;
        String serieRps;
        String numeroRps;
        String dataCompetencia;
        String codigoTributacaoNacional;
        String codigoAtividadeEconomica;
        String codigoMunicipioPrestacao;
        String codigoNbs;
        String discriminacao;
        String valorServico;
        String aliquotaIss;
        boolean issRetido;
        String optanteSimplesNacional;
        String telefonePrestador;
        String emailPrestador;
        String logradouroPrestador;
        String numeroPrestador;
        String complementoPrestador;
        String bairroPrestador;
        String cepPrestador;
        String cpfTomador;
        String cnpjTomador;
        String nomeTomador;
        String telefoneTomador;
        String emailTomador;
        String logradouroTomador;
        String numeroTomador;
        String complementoTomador;
        String bairroTomador;
        String codigoMunicipioTomador;
        String cepTomador;
    }

    public static final class Cancelamento {
        String chaveNfse;
        String cnpjAutor;
        String motivo;
    }
}
