package br.com.brasil_saas.cadastro.controller;

import br.com.brasil_saas.cadastro.model.Municipio;
import br.com.brasil_saas.cadastro.service.MunicipioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/municipios")
@RequiredArgsConstructor
public class MunicipioController {

    private final MunicipioService service;

    @GetMapping
    public Page<Municipio> listar(
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return service.listar(pageable);
    }

    @GetMapping("/buscar")
    public Page<Municipio> buscar(
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String termo,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return service.buscar(codigo, nome, termo, pageable);
    }

    @GetMapping("/codigo/{codigoIbge}")
    public ResponseEntity<Municipio> buscarPorCodigo(@PathVariable String codigoIbge) {
        return service.buscarPorCodigo(codigoIbge)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Municipio> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Municipio> criar(@RequestBody Municipio municipio) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(municipio));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Municipio> atualizar(
            @PathVariable Long id,
            @RequestBody Municipio municipio) {
        return ResponseEntity.ok(service.atualizar(id, municipio));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
