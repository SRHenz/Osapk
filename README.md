# Ordens de Serviço (OSApp)

App Android para empresa de serviço/manutenção: cadastro de cliente, solicitação
do serviço e conclusão com materiais utilizados. Cada OS é salva localmente
como um arquivo `.osrv` (JSON), que pode ser compartilhado pelo WhatsApp e
reaberto direto no app pra edição.

## Como subir pro GitHub pelo Termux

1. Extraia o zip no celular (ex: em `~/storage/downloads/osapp` ou onde preferir).
2. No Termux, entre na pasta do projeto:
   ```
   cd caminho/para/osapp
   ```
3. Configure git (só na primeira vez):
   ```
   git config --global user.name "SEU_NOME"
   git config --global user.email "seu@email.com"
   ```
4. Inicialize e suba pro repositório:
   ```
   git init
   git add .
   git commit -m "Primeira versão do OSApp"
   git branch -M main
   git remote add origin https://github.com/SEU_USUARIO/SEU_REPOSITORIO.git
   git push -u origin main
   ```
   (troque `SEU_USUARIO` e `SEU_REPOSITORIO`; se pedir senha, use um
   Personal Access Token do GitHub no lugar da senha).

## Baixar o APK compilado

1. No GitHub, abra a aba **Actions** do repositório.
2. Espere o workflow "Build APK" terminar (ícone verde ✓).
3. Entre na execução e baixe o artefato **OSApp-debug-apk** — é o `.apk`
   pronto pra instalar no celular.

## Extensão do arquivo

Escolhi `.osrv` (Ordem de ServiRVo/serviço) porque não encontrei nenhum
programa conhecido usando essa extensão — evita conflito de "abrir com" no
Android. O app já registra automaticamente o `.osrv` como tipo de arquivo
dele: ao tocar num `.osrv` baixado do WhatsApp, o Android oferece abrir com
o app, que carrega a OS pronta pra editar.

## Próximos passos possíveis
- Histórico de alterações por técnico
- Assinatura do cliente na conclusão
- Fotos anexadas à OS
- Sincronização em nuvem (fase 2)
