#!/usr/bin/env python3
"""
Gerador automático de testes unitários para Brasil SaaS ERP
Gera testes para Controllers e Services usando JUnit5 + Mockito
"""

import os
import re
from pathlib import Path

BASE_DIR = Path("/workspace")
SRC_MAIN = BASE_DIR / "src/main/java/br/com/brasil_saas"
SRC_TEST = BASE_DIR / "src/test/java/br/com/brasil_saas"

def extract_class_name(file_path):
    """Extrai nome da classe do arquivo Java"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
        match = re.search(r'public\s+(?:class|interface)\s+(\w+)', content)
        return match.group(1) if match else None

def extract_package(file_path):
    """Extrai package do arquivo Java"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
        match = re.search(r'package\s+([\w.]+);', content)
        return match.group(1) if match else None

def get_model_name(service_name):
    """Inferir nome do model baseado no service"""
    # Remove "Service" do final
    base_name = service_name.replace("Service", "")
    return base_name

def generate_controller_test(controller_name, package_name, service_name, model_name):
    """Gera código de teste para controller"""
    
    # Capitaliza primeira letra para variáveis
    model_var = model_name[0].lower() + model_name[1:] if model_name else "objeto"
    service_var = service_name[0].lower() + service_name[1:] if service_name else "service"
    
    test_code = f"""package {package_name.replace('.controller', '.controller')};

import {package_name.replace('.controller', '.dto')}.{model_name}Request;
import {package_name.replace('.controller', '.model')}.{model_name};
import {package_name.replace('.controller', '.service')}.{service_name};
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class {controller_name}Test {{

    @Mock
    private {service_name} {service_var};

    @InjectMocks
    private {controller_name} {service_var.replace('Service', 'Controller')};

    private {model_name}Request request;
    private {model_name} {model_var};

    @BeforeEach
    void setUp() {{
        request = new {model_name}Request();
        request.setNome("Teste");

        {model_var} = new {model_name}();
        {model_var}.setId(1L);
        {model_var}.setNome("Teste");
    }}

    @Test
    void testCriar_Success() {{
        when({service_var}.criar(any({model_name}Request.class))).thenReturn({model_var});

        ResponseEntity<{model_name}> response = {service_var.replace('Service', 'Controller')}.criar(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify({service_var}, times(1)).criar(any({model_name}Request.class));
    }}

    @Test
    void testBuscarPorId_Success() {{
        when({service_var}.buscarPorId(anyLong())).thenReturn({model_var});

        ResponseEntity<{model_name}> response = {service_var.replace('Service', 'Controller')}.buscarPorId(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify({service_var}, times(1)).buscarPorId(anyLong());
    }}

    @Test
    void testBuscarPorId_NotFound() {{
        when({service_var}.buscarPorId(anyLong())).thenThrow(new ResourceNotFoundException("Nao encontrado"));

        assertThrows(ResourceNotFoundException.class, () -> {{
            {service_var.replace('Service', 'Controller')}.buscarPorId(999L);
        }});
    }}

    @Test
    void testListar_Success() {{
        List<{model_name}> lista = Arrays.asList({model_var});
        when({service_var}.listar()).thenReturn(lista);

        ResponseEntity<List<{model_name}>> response = {service_var.replace('Service', 'Controller')}.listar();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify({service_var}, times(1)).listar();
    }}

    @Test
    void testAtualizar_Success() {{
        when({service_var}.atualizar(anyLong(), any({model_name}Request.class))).thenReturn({model_var});

        ResponseEntity<{model_name}> response = {service_var.replace('Service', 'Controller')}.atualizar(1L, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify({service_var}, times(1)).atualizar(anyLong(), any({model_name}Request.class));
    }}

    @Test
    void testExcluir_Success() {{
        doNothing().when({service_var}).excluir(anyLong());

        ResponseEntity<Void> response = {service_var.replace('Service', 'Controller')}.excluir(1L);

        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        verify({service_var}, times(1)).excluir(anyLong());
    }}
}}
"""
    return test_code

def generate_service_test(service_name, package_name, model_name, repository_name):
    """Gera código de teste para service"""
    
    model_var = model_name[0].lower() + model_name[1:] if model_name else "objeto"
    repo_var = repository_name[0].lower() + repository_name[1:] if repository_name else "repository"
    service_var = service_name[0].lower() + service_name[1:] if service_name else "service"
    
    test_code = f"""package {package_name};

import {package_name.replace('.service', '.model')}.{model_name};
import {package_name.replace('.service', '.repository')}.{repository_name};
import {package_name}.dto.{model_name}Request;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class {service_name}Test {{

    @Mock
    private {repository_name} {repo_var};

    @InjectMocks
    private {service_name} {service_var};

    private {model_name}Request request;
    private {model_name} {model_var};

    @BeforeEach
    void setUp() {{
        request = new {model_name}Request();
        request.setNome("Teste");

        {model_var} = new {model_name}();
        {model_var}.setId(1L);
        {model_var}.setNome("Teste");
    }}

    @Test
    void testCriar_Success() {{
        when({repo_var}.save(any({model_name}.class))).thenReturn({model_var});

        {model_name} resultado = {service_var}.criar(request);

        assertNotNull(resultado);
        assertEquals("Teste", resultado.getNome());
        verify({repo_var}, times(1)).save(any({model_name}.class));
    }}

    @Test
    void testBuscarPorId_Success() {{
        when({repo_var}.findById(anyLong())).thenReturn(Optional.of({model_var}));

        {model_name} resultado = {service_var}.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals("Teste", resultado.getNome());
        verify({repo_var}, times(1)).findById(anyLong());
    }}

    @Test
    void testBuscarPorId_NotFound() {{
        when({repo_var}.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {{
            {service_var}.buscarPorId(999L);
        }});
    }}

    @Test
    void testListar_Success() {{
        List<{model_name}> lista = Arrays.asList({model_var});
        when({repo_var}.findAll()).thenReturn(lista);

        List<{model_name}> resultado = {service_var}.listar();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify({repo_var}, times(1)).findAll();
    }}

    @Test
    void testAtualizar_Success() {{
        when({repo_var}.findById(anyLong())).thenReturn(Optional.of({model_var}));
        when({repo_var}.save(any({model_name}.class))).thenReturn({model_var});

        {model_name} resultado = {service_var}.atualizar(1L, request);

        assertNotNull(resultado);
        verify({repo_var}, times(1)).save(any({model_name}.class));
    }}

    @Test
    void testExcluir_Success() {{
        when({repo_var}.existsById(anyLong())).thenReturn(true);
        doNothing().when({repo_var}).deleteById(anyLong());

        {service_var}.excluir(1L);

        verify({repo_var}, times(1)).deleteById(anyLong());
    }}
}}
"""
    return test_code

def find_controllers():
    """Encontra todos os controllers no projeto"""
    controllers = []
    for root, dirs, files in os.walk(SRC_MAIN):
        for file in files:
            if file.endswith('Controller.java'):
                controllers.append(Path(root) / file)
    return controllers

def find_services():
    """Encontra todos os services no projeto"""
    services = []
    for root, dirs, files in os.walk(SRC_MAIN):
        for file in files:
            if file.endswith('Service.java') and not file.endswith('Test.java'):
                services.append(Path(root) / file)
    return services

def main():
    print("=" * 60)
    print("GERADOR DE TESTES UNITÁRIOS - BRASIL-SAAS ERP")
    print("=" * 60)
    
    created_tests = []
    
    # Processar Controllers
    print("\n📋 PROCESSANDO CONTROLLERS...")
    controllers = find_controllers()
    print(f"   Encontrados {len(controllers)} controllers")
    
    for ctrl_path in controllers:
        ctrl_name = extract_class_name(ctrl_path)
        package = extract_package(ctrl_path)
        
        if not ctrl_name or not package:
            continue
        
        # Inferir service e model
        model_name = ctrl_name.replace("Controller", "")
        service_name = model_name + "Service"
        
        # Criar diretório de teste
        test_dir = SRC_TEST / "/".join(package.split('.')[3:]) / "controller"
        test_dir.mkdir(parents=True, exist_ok=True)
        
        test_file = test_dir / f"{ctrl_name}Test.java"
        
        if test_file.exists():
            print(f"   ⏭️  {ctrl_name}Test.java já existe")
            continue
        
        # Gerar teste
        test_code = generate_controller_test(ctrl_name, package, service_name, model_name)
        
        try:
            with open(test_file, 'w', encoding='utf-8') as f:
                f.write(test_code)
            created_tests.append(str(test_file))
            print(f"   ✅ Criado: {ctrl_name}Test.java")
        except Exception as e:
            print(f"   ❌ Erro ao criar {ctrl_name}Test.java: {e}")
    
    # Processar Services
    print("\n📋 PROCESSANDO SERVICES...")
    services = []
    for root, dirs, files in os.walk(SRC_MAIN):
        if '/service/' in root:
            for file in files:
                if file.endswith('Service.java') and not file.endswith('Test.java'):
                    services.append(Path(root) / file)
    
    print(f"   Encontrados {len(services)} services")
    
    for svc_path in services:
        svc_name = extract_class_name(svc_path)
        package = extract_package(svc_path)
        
        if not svc_name or not package:
            continue
        
        # Inferir model e repository
        model_name = svc_name.replace("Service", "")
        repo_name = model_name + "Repository"
        
        # Criar diretório de teste
        test_dir = SRC_TEST / "/".join(package.split('.')[3:]) / "service"
        test_dir.mkdir(parents=True, exist_ok=True)
        
        test_file = test_dir / f"{svc_name}Test.java"
        
        if test_file.exists():
            print(f"   ⏭️  {svc_name}Test.java já existe")
            continue
        
        # Gerar teste
        test_code = generate_service_test(svc_name, package, model_name, repo_name)
        
        try:
            with open(test_file, 'w', encoding='utf-8') as f:
                f.write(test_code)
            created_tests.append(str(test_file))
            print(f"   ✅ Criado: {svc_name}Test.java")
        except Exception as e:
            print(f"   ❌ Erro ao criar {svc_name}Test.java: {e}")
    
    print("\n" + "=" * 60)
    print(f"TOTAL DE TESTES CRIADOS: {len(created_tests)}")
    print("=" * 60)
    
    if created_tests:
        print("\n📁 Arquivos criados:")
        for test in created_tests[:20]:  # Mostrar primeiros 20
            print(f"   - {test}")
        if len(created_tests) > 20:
            print(f"   ... e mais {len(created_tests) - 20} arquivos")

if __name__ == "__main__":
    main()
