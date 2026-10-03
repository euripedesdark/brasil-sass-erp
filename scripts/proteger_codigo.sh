#!/bin/bash
# Script para proteger o código-fonte do JAR

echo "Protegendo o código-fonte do JAR..."

# Verifica se o JAR existe
if [ ! -f "target/brasil_saas-erp-1.0.0-SNAPSHOT.jar" ]; then
    echo "Erro: Arquivo JAR não encontrado!"
    exit 1
fi

# Cria diretório para o JAR protegido
mkdir -p protected

# Copia o JAR original para o diretório protegido
cp target/brasil_saas-erp-1.0.0-SNAPSHOT.jar protected/

echo "JAR copiado para proteção."

# Se tivermos o ProGuard disponível, podemos executá-lo
if command -v proguard &> /dev/null; then
    echo "ProGuard encontrado. Executando ofuscação..."
    
    # Configuração básica do ProGuard
    cat > proguard.conf << EOF
-injars protected/brasil_saas-erp-1.0.0-SNAPSHOT.jar
-outjars protected/brasil_saas-erp-protected.jar
-libraryjars <java.home>/lib/rt.jar
-libraryjars <java.home>/lib/jce.jar

# Não ofuscar classes Spring Boot
-keep class org.springframework.boot.** { *; }
-keep class org.springframework.web.** { *; }
-keep class org.springframework.security.** { *; }
-keep class javax.** { *; }
-keep class org.hibernate.** { *; }
-keep class com.fasterxml.** { *; }

# Manter anotações importantes
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses

# Manter classes com anotações Spring
-keep @org.springframework.stereotype.Component class *
-keep @org.springframework.stereotype.Service class *
-keep @org.springframework.stereotype.Repository class *
-keep @org.springframework.web.bind.annotation.RestController class *
-keep @javax.persistence.* class *
-keep @jakarta.persistence.* class *
EOF

    proguard @proguard.conf
    
    if [ -f "protected/brasil_saas-erp-protected.jar" ]; then
        echo "Código ofuscado com sucesso!"
        rm -f protected/brasil_saas-erp-1.0.0-SNAPSHOT.jar
        echo "JAR protegido: protected/brasil_saas-erp-protected.jar"
    else
        echo "Erro ao ofuscar o código."
        echo "JAR original mantido: protected/brasil_saas-erp-1.0.0-SNAPSHOT.jar"
    fi
    
    rm -f proguard.conf
else
    echo "ProGuard não encontrado. O JAR será mantido sem ofuscação."
    echo "Para proteger o código, instale o ProGuard ou outra ferramenta de ofuscação."
    echo "JAR disponível em: protected/brasil_saas-erp-1.0.0-SNAPSHOT.jar"
fi

echo "Proteção concluída."