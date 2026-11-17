
package br.org.edu.ifrn.carros.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Service
public class FotoService {

    private static final Logger log = LoggerFactory.getLogger(FotoService.class);
    private static final long LIMITE = 5L * 1024 * 1024;

    private final Path diretorio;

    public FotoService(@Value("${app.imagens.diretorio}") String caminho) {
        this.diretorio = Path.of(caminho).toAbsolutePath().normalize();
    }

    public String salvar(MultipartFile foto) {
        if (foto.isEmpty() || foto.getSize() > LIMITE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Fotografia vazia ou maior que 5 MB.");
        }

        try {
            byte[] conteudo = foto.getBytes();
            String extensao = identificarFormato(conteudo);
            String nome = UUID.randomUUID() + "." + extensao;

            Files.createDirectories(diretorio);
            Files.write(diretorio.resolve(nome), conteudo, StandardOpenOption.CREATE_NEW);

            return nome;
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao salvar fotografia.", e);
        }
    }

    public Resource carregar(String nome) {
        if (nome == null || !nome.matches("[0-9a-f-]{36}\\.(jpg|png|webp)")) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Fotografia não encontrada.");
        }

        Path arquivo = diretorio.resolve(nome);

        if (!Files.isRegularFile(arquivo)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Fotografia não encontrada.");
        }

        return new FileSystemResource(arquivo);
    }

    public void apagar(String nome) {
        try {
            Files.deleteIfExists(diretorio.resolve(nome));
        } catch (IOException e) {
            log.warn("Não foi possível remover a fotografia {}", nome, e);
        }
    }

    private String identificarFormato(byte[] dados) {
        if (dados.length >= 3
                && dados[0] == (byte) 0xFF
                && dados[1] == (byte) 0xD8
                && dados[2] == (byte) 0xFF) {
            return "jpg";
        }

        if (dados.length >= 8
                && dados[0] == (byte) 0x89
                && dados[1] == 0x50
                && dados[2] == 0x4E
                && dados[3] == 0x47
                && dados[4] == 0x0D
                && dados[5] == 0x0A
                && dados[6] == 0x1A
                && dados[7] == 0x0A) {
            return "png";
        }

        if (dados.length >= 12
                && dados[0] == 'R'
                && dados[1] == 'I'
                && dados[2] == 'F'
                && dados[3] == 'F'
                && dados[8] == 'W'
                && dados[9] == 'E'
                && dados[10] == 'B'
                && dados[11] == 'P') {
            return "webp";
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Formato de imagem não permitido.");
    }
}
