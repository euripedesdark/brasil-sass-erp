package br.com.brasil_saas.shared.service;

import br.com.brasil_saas.shared.dto.ImagemDto;
import br.com.brasil_saas.shared.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ImagemService {

    private static final List<String> TIPOS_IMAGEM_PERMITIDOS = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    private static final long TAMANHO_MAXIMO = 10 * 1024 * 1024; // 10MB

    public ImagemDto processarImagem(MultipartFile arquivo) throws IOException {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new BusinessException("Nenhum arquivo foi enviado");
        }

        // Validar tipo de conteúdo
        String contentType = arquivo.getContentType();
        if (contentType == null || !TIPOS_IMAGEM_PERMITIDOS.contains(contentType.toLowerCase())) {
            throw new BusinessException("Tipo de arquivo inválido. Tipos permitidos: JPEG, PNG, GIF, WEBP");
        }

        // Validar tamanho do arquivo
        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new BusinessException("Tamanho do arquivo excede o limite permitido de 10MB");
        }

        // Converter para byte array
        byte[] conteudo = arquivo.getBytes();

        // Criar DTO com informações da imagem
        ImagemDto dto = new ImagemDto();
        dto.setTipoConteudo(contentType);
        dto.setTamanho(arquivo.getSize());

        return dto;
    }
    
    public byte[] converterParaByteArray(MultipartFile arquivo) throws IOException {
        return arquivo.getBytes();
    }
}