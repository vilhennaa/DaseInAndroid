# Dase.in (Projeto Final de MBaaS)

**Autor: Gustavo Cotovicz**

## Sumário

* [1. Visão Geral do Projeto](https://www.google.com/search?q=%231-vis%C3%A3o-geral-do-projeto)
* [2. Funcionalidades Implementadas (Conforme Rubrica)](https://www.google.com/search?q=%232-funcionalidades-implementadas-conforme-rubrica)
    * [Autenticação (Firebase Authentication)](https://www.google.com/search?q=%23autentica%C3%A7%C3%A3o-firebase-authentication)
    * [Banco de Dados (Cloud Firestore)](https://www.google.com/search?q=%23banco-de-dados-cloud-firestore)
    * [Outros Serviços Cloud (Storage & Messaging)](https://www.google.com/search?q=%23outros-servi%C3%A7os-cloud-storage--messaging)
* [3. Arquitetura e Tecnologias](https://www.google.com/search?q=%233-arquitetura-e-tecnologias)
* [4. Como Executar o Projeto (Instruções)](https://www.google.com/search?q=%234-como-executar-o-projeto-instru%C3%A7%C3%B5es)
* [5. Estrutura do Código](https://www.google.com/search?q=%235-estrutura-do-c%C3%B3digo)

-----

## 1\. Visão Geral do Projeto

**Dase.in** é uma aplicação móvel Android (nativa em Kotlin/Compose) concebida como um "porto seguro" para indivíduos com altas habilidades/superdotação.

O projeto ataca o problema do isolamento intelectual, oferecendo um espaço curado onde mentes complexas podem partilhar as suas criações, "sementes de projetos", pensamentos profundos e artes, e encontrar validação e conexão com pares intelectuais.

## 2\. Funcionalidades Implementadas (Conforme Rubrica)

Este projeto implementa com sucesso todos os requisitos funcionais da disciplina, utilizando o Firebase como MBaaS (Mobile Backend as a Service).

### Autenticação (Firebase Authentication)

* **Registo de Novos Utilizadores:** Fluxo completo de criação de conta com e-mail e senha.
* **Login de Utilizadores:** Sistema de login funcional com gestão de estado (o utilizador mantém-se logado).
* **Logout:** Funcionalidade de "Sair" na tela de Perfil.

### Banco de Dados (Cloud Firestore)

A aplicação utiliza o Cloud Firestore para um CRUD (Criar, Ler, Atualizar, Apagar) completo e robusto em três coleções principais: `creations`, `comments`, e `users`.

* **CRUD de Posts (Criações):**
    * Utilizadores podem criar, ler, atualizar e apagar os seus próprios *posts*.
    * Cada *post* armazena o `authorName` (o "Nome de Exibição" do perfil) e não o email.
* **CRUD de Comentários:**
    * Utilizadores podem comentar em *posts*.
    * Suporta **respostas aninhadas** (lógica de `parentId`).
    * **Contador atómico:** O `commentCount` no *post* principal é atualizado atomicamente (`FieldValue.increment`) no *backend* sempre que um novo comentário é feito.
    * **Segurança:** Utilizadores só podem editar ou apagar os seus próprios comentários.
* **CRUD de Perfis de Utilizador:**
    * **Criação automática:** Um perfil de utilizador (na coleção `users`) é criado automaticamente quando um novo utilizador se regista.
    * **Edição de Perfil:** O utilizador pode editar o seu "Nome de Exibição" e "Bio".
* **Funcionalidades Adicionais (Bónus):**
    * **Sistema de "Salvar Posts":** O utilizador pode "salvar" (marcar) *posts* de outros. Esta lista (`savedPostIds`) é guardada no seu perfil.
    * **Sistema de Tags (Filtro):**
        * Uma coleção `config/tags` no Firestore armazena as *tags* pré-definidas.
        * O utilizador seleciona *Chips* (ex: "Kotlin", "Programação") ao criar um *post*.
        * O Feed Principal (`FeedScreen`) permite filtrar *posts* por texto (título, autor) E pelas *tags* selecionadas numa tela de filtro separada.
    * **Abas no Perfil:** A tela de Perfil contém abas para o utilizador ver "Minhas Publicações" e "Posts Salvos".

### Outros Serviços Cloud (Storage & Messaging)

O projeto integra com sucesso dois outros serviços cloud do Firebase, cumprindo (e excedendo) o requisito:

* **Firebase Storage (Upload de Imagens):**
    * Na `CreateScreen`, o utilizador pode selecionar uma imagem da galeria do dispositivo.
    * A aplicação faz o *upload* do ficheiro para o Firebase Storage.
    * A URL de *download* é guardada no Cloud Firestore junto com o *post*.
    * As imagens são exibidas no *feed* e na tela de detalhes (utilizando a biblioteca **Coil**).
* **Firebase Cloud Messaging (Notificações Push):**
    * A aplicação pede permissão (`POST_NOTIFICATIONS`) no Android 13+.
    * Foi implementado um `FirebaseMessagingService` que "ouve" e constrói notificações.
    * A aplicação recebe com sucesso notificações *push* enviadas a partir do painel do Firebase Console.

## 3\. Arquitetura e Tecnologias

* **Linguagem:** 100% [Kotlin](https://kotlinlang.org/)
* **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (declarativa e moderna)
* **Arquitetura:** **MVVM (Model-View-ViewModel)**
    * A lógica de negócio é isolada em `ViewModels`.
    * A lógica de dados é abstraída pelo **Padrão Repository** (`CreationRepository`, `UserProfileRepository`).
    * A UI (`Screens`) apenas reage a `StateFlow`s.
* **Backend (MBaaS):** [Firebase](https://firebase.google.com/)
    * **Autenticação:** Firebase Authentication
    * **Base de Dados:** Cloud Firestore
    * **Armazenamento de Ficheiros:** Firebase Storage
    * **Notificações:** Firebase Cloud Messaging
* **Bibliotecas Adicionais:**
    * **Navegação:** Compose Navigation
    * **Carregamento de Imagem:** [Coil](https://coil-kt.github.io/coil/) (para `AsyncImage`)

## 4\. Como Executar o Projeto (Instruções)

Para executar este projeto, são necessários 4 passos de configuração do Firebase, pois o ficheiro `google-services.json` não está incluído no repositório.

### Passo 1: Clonar e Abrir

1.  Clone este repositório.
2.  Abra o projeto no Android Studio.

### Passo 2: Conectar o Firebase

1.  Aceda ao [Firebase Console](https://console.firebase.google.com/).
2.  Crie um novo projeto.
3.  Adicione uma aplicação Android ao projeto (clique no ícone do Android).
4.  Use o `applicationId` (namespace) do projeto: **`com.cotovicz.daseinandroid`**.
5.  Faça o download do ficheiro **`google-services.json`** e coloque-o dentro da pasta **`app/`** do projeto no Android Studio.

### Passo 3: Configurar os Serviços

1.  **Authentication:** No painel do Firebase, vá a "Authentication" e ative o provedor **"E-mail/senha"**.
2.  **Firestore:**
    * Vá a "Firestore Database" e crie uma nova base de dados.
    * **Crie o documento de Tags:**
        * Coleção: `config`
        * Documento: `tags`
        * Campo 1: `availableTags` (Tipo: `array`)
        * Adicione *strings* a este *array* (ex: "Kotlin", "Programação", "Arte").
    * **Crie os Índices (Obrigatório):** A aplicação irá "crashar" sem estes índices.
        * Vá ao separador "Índices" e crie dois índices compostos:
      <!-- end list -->
        1.  **Índice 1 (Comentários):**
            * Coleção: `comments`
            * Campos: `creationId` (Ascendente) e `timestamp` (Ascendente)
        2.  **Índice 2 (Posts do Perfil):**
            * Coleção: `creations`
            * Campos: `userId` (Ascendente) e `timestamp` (Descendente)
3.  **Storage:**
    * Vá a "Storage" e clique em "Começar".
    * Escolha "Modo de Teste" (será alterado a seguir).
    * Escolha uma localização (ex: `us-central1`).
    * Assim que for criado, vá ao separador **"Regras"** e substitua o conteúdo por:
    <!-- end list -->
    ```firebase-rules
    rules_version = '2';
    service firebase.storage {
      match /b/{bucket}/o {
        match /{allPaths=**} {
          allow read: if true;
        }
        match /images/{userId}/{allPaths=**} {
          allow write: if request.auth != null && request.auth.uid == userId;
        }
      }
    }
    ```
    * Clique em "Publicar".
4.  **Cloud Messaging:** Este serviço funciona automaticamente após adicionar o `google-services.json`.

### Passo 4: Executar

1.  Volte ao Android Studio. O Gradle *sync* deve ter sido feito.
2.  Execute a aplicação (Run 'app').
3.  **Crie um novo utilizador** (o perfil no Firestore será criado automaticamente).

## 5\. Estrutura do Código

O projeto está organizado da seguinte forma:

* **`data/remote/models`**: Contém as `data classes` (POJOs) que representam os documentos do Firestore (`Creation`, `Comment`, `UserProfile`).
* **`data/repository`**: Contém os Repositórios, que são a única fonte da verdade para os dados. Eles lidam com toda a lógica de falar com o Firestore e o Storage.
* **`ui/viewmodel`**: Contém os `ViewModels` (`AuthViewModel`, `CreationViewModel`), que gerem o estado da UI e orquestram a lógica de negócio (ex: "fazer upload ANTES de criar o post").
* **`ui/screens`**: Contém os ecrãs `@Composable` (ex: `FeedScreen`, `CreateScreen`, `ProfileScreen`).
* **`ui/components`**: Contém componentes reutilizáveis (ex: `CreationCard`, `AppTopBar`).
* **`navigation`**: Contém o `AppNavHost`, que define o gráfico de navegação da aplicação.
* **`MyFirebaseMessagingService`**: O serviço que recebe as notificações push.