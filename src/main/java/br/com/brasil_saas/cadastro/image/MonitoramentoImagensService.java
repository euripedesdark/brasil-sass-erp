package br.com.brasil_saas.cadastro.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoramentoImagensService {
    private final ProdutoImagemService imagemService;

    @Value("${brasil-saas.imagens.diretorio-entrada:src/main/resources/static/images}")
    private String diretorioEntrada;

    @PostConstruct
    void iniciarMonitoramento() {
        Thread thread = new Thread(this::monitorarEventos);
        thread.setName("monitor-imagens-mongodb");
        thread.setDaemon(true);
        thread.start();
    }

    private void monitorarEventos() {
        Path entrada = Path.of(diretorioEntrada);
        try {
            Files.createDirectories(entrada);
            try (WatchService watchService = entrada.getFileSystem().newWatchService()) {
                entrada.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
                while (!Thread.currentThread().isInterrupted()) {
                    var key = watchService.take();
                    for (var event : key.pollEvents()) {
                        Path arquivo = entrada.resolve((Path) event.context());
                        if (Files.isRegularFile(arquivo) && ehImagem(arquivo)) {
                            importar(arquivo);
                        }
                    }
                    key.reset();
                }
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Não foi possível monitorar a pasta de imagens {}", entrada, e);
        }
    }

    @Scheduled(fixedDelayString = "${brasil-saas.imagens.monitoramento-intervalo-ms:5000}")
    void vincularImagensPendentes() {
        imagemService.vincularPendentes();
    }

    private boolean ehImagem(Path arquivo) {
        String nome = arquivo.getFileName().toString().toLowerCase(Locale.ROOT);
        return nome.endsWith(".jpg") || nome.endsWith(".jpeg") || nome.endsWith(".png") || nome.endsWith(".gif") || nome.endsWith(".webp");
    }

    private void importar(Path arquivo) {
        try {
            String tipo = Files.probeContentType(arquivo);
            if (tipo == null || !tipo.startsWith("image/")) {
                tipo = "image/" + extensao(arquivo);
            }
            byte[] conteudo = Files.readAllBytes(arquivo);
            String contentType = tipo;
            imagemService.localizarProduto(arquivo.getFileName().toString())
                    .ifPresentOrElse(produto -> imagemService.salvar(produto, arquivo.getFileName().toString(), contentType, conteudo, false),
                            () -> imagemService.armazenarPendente(arquivo.getFileName().toString(), contentType, conteudo));
        } catch (Exception e) {
            log.warn("Falha ao importar imagem {}.", arquivo, e);
        }
    }

    private String extensao(Path arquivo) {
        String nome = arquivo.getFileName().toString();
        return nome.substring(nome.lastIndexOf('.') + 1);
    }
}
