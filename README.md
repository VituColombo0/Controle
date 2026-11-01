# Controle: Mindful Screen Time & App Blocker

Aplicativo Android nativo desenvolvido em Kotlin e Jetpack Compose, focado no combate ao uso compulsivo do celular através de pausas conscientes com respiração guiada, reflexão de intenções e bloqueio seletivo de feeds curtos (Reels e Shorts).

[![Platform: Android](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-brightgreen.svg?logo=android&logoColor=white)](https://developer.android.com/)
[![Language: Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![UI: Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![Theme: Pure OLED Dark](https://img.shields.io/badge/Design-Pure%20Black%20%2B%20Mint-darkgreen.svg)](https://github.com/VituColombo0/Controle)
[![License: MIT](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

---

## Visão Geral

Diferentemente de bloqueadores convencionais que simplesmente trancam o aplicativo gerando frustração ou burla imediata, o **Controle** atua no circuito de recompensa da dopamina. Ao tentar abrir um aplicativo distrativo, o sistema intercepta a inicialização e introduz uma barreira de atrito consciente: um exercício respiratório guiado de desaceleração seguido por um questionamento reflexivo de propósito.

```
                    Tentativa de Abertura do App Distrativo
                                       │
                                       ▼
                  [ Interceptação por AccessibilityService ]
                     Detecta app na lista de restrição
                                       │
                                       ▼
                   [ Overlay de Pausa Consciente ]
                    (Exibição via SYSTEM_ALERT_WINDOW)
                                       │
                  ┌────────────────────┴────────────────────┐
                  ▼                                         ▼
      [ Exercício Respiratório ]                [ Reflexão de Intenções ]
       Inspire (4s) ── Segure (2s)                "Qual é o seu propósito
       ── Expire (4s) com vibração haptics         agora neste app?"
                  │                                         │
                  └────────────────────┬────────────────────┘
                                       │
                  ┌────────────────────┴────────────────────┐
                  ▼                                         ▼
       [ "Mudei de Ideia" ]                       [ Passe Temporário ]
     Fecha o app imediatamente,                Concede 5 ou 15 min após
    soma minutos poupados e streak             digitação de frase consciente
```

---

## Funcionalidades e Mecânicas de Atenção

### 1. Pausa Consciente com Respiração Guiada
- **Ciclo Trifásico:** Animação respiratória circular compassada: *Inspire* (4 segundos), *Segure* (2 segundos) e *Expire* (4 segundos).
- **Feedback Háptico Suave:** Vibrações táteis sincronizadas com as transições respiratórias, ancorando a atenção do usuário no mundo físico.
- **Temporização Ininterrupta:** Impede o toque rápido de dispersão, quebrando o reflexo automático de abertura de tela.

### 2. Reflexão de Intenção
- Pergunta reflexiva obrigatória antes de qualquer liberação: *"Qual é o seu objetivo real agora?"*.
- Seleção rápida de intenções: Trabalho/Estudo, Responder Mensagem Urgente, Tédio ou Hábito Automático.

### 3. Ação Heroica "Mudei de Ideia"
- Um botão proeminente que encerra a tentativa de uso, devolve o usuário diretamente à tela inicial do Android via serviço de acessibilidade e computa o tempo poupado no histórico de dias conscientes (*Streak*).

### 4. Passe Temporário com Expiração Rígida
- Liberação gradual de 5 ou 15 minutos que exige, no modo estrito, a digitação de uma frase afirmativa (*"Eu escolho conscientemente abrir este app"*).
- Após o término do tempo, a tela de pausa retorna automaticamente.

### 5. Bloqueio Cirúrgico de Reels e Shorts
- O leitor de acessibilidade monitora exclusivamente as abas e contêineres de vídeos curtos verticais no Instagram e YouTube, permitindo o uso normal de conversas (Direct/WhatsApp) e pesquisas produtivas.

### 6. Sessões de Foco & Offtime
- Temporizadores dedicados para trabalho profundo: Pomodoro (25 minutos), Foco Profundo (50 minutos) e Detox Digital (120 minutos).

### 7. Interface Minimalista OLED
- Construída em preto absoluto (`#000000`) para economia de bateria em telas AMOLED, com contraste em acentos menta neon (`#10B981`) e tipografia enxuta sem poluição visual.

---

## Permissões Necessárias no Android

Para exercer a interceptação no momento do lançamento sem comprometer a estabilidade do sistema operacional, o app requer as seguintes autorizações:

| Permissão | Identificador no Android | Finalidade Técnica |
| :--- | :--- | :--- |
| **Acessibilidade** | `AccessibilityService` | Identifica a transição de janela dos apps bloqueados e executa a ação de fechar ao desistir. |
| **Acesso ao Uso** | `PACKAGE_USAGE_STATS` | Contabiliza o tempo diário decorrido em cada app e valida cotas máximas. |
| **Sobreposição** | `SYSTEM_ALERT_WINDOW` | Renderiza a camada de respiração consciente acima do app bloqueado. |
| **Controle de Vibração** | `VIBRATE` | Fornece resposta háptica sutil durante as três fases da animação de respiração. |

---

## Instalação e Execução

### Instalação Direta via APK Pré-compilado

O instalador compilado está disponível diretamente no repositório:
- [Download do Arquivo Controle.apk](Controle.apk)

1. Baixe o arquivo `Controle.apk` em seu dispositivo Android.
2. Permita a instalação de fontes desconhecidas nas configurações do sistema.
3. Abra o aplicativo e conceda as permissões de Acessibilidade e Sobreposição indicadas na tela inicial de configuração.

---

## Compilação a partir do Código-Fonte

### Pré-requisitos
- Android Studio Ladybug ou superior
- JDK 17
- Android SDK com suporte para API 26 (Android 8.0) até API 34 (Android 14)

### Linha de Comando (Gradle Wrapper)

```bash
# Clone o repositório
git clone https://github.com/VituColombo0/Controle.git
cd Controle

# Compilar versão de depuração (Debug APK)
./gradlew assembleDebug

# O APK gerado estará em: app/build/outputs/apk/debug/app-debug.apk
```

---

## Estrutura do Repositório

```text
Controle/
├── app/
│   ├── build.gradle.kts       # Dependências do Jetpack Compose e plugins Android
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml # Declaração de serviços e permissões
│           ├── java/               # Código-fonte Kotlin (Services, UI e ViewModel)
│           └── res/                # Recursos de tema, strings, drawables e ícones
├── build.gradle.kts           # Configuração de build raiz
├── settings.gradle.kts        # Declaração de módulos e repositórios
├── gradle.properties          # Flags de memória JVM e AndroidX
├── Controle.apk               # Pacote APK compilado para instalação direta
└── README.md                  # Documentação técnica
```

---

## Licença

Distribuído sob a licença MIT. Consulte [LICENSE](LICENSE) para mais detalhes.
