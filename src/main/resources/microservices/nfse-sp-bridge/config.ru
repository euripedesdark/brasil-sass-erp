# Ponto de entrada Rack do nfse-sp-bridge.
#
# O app.rb e Sinatra classico (nao modular), entao o Rack::Builder nao acha a
# classe `App` so pelo require. O `run Sinatra::Application` resolve, e e o
# que permite subir com `rackup` em vez do puma direto.
require_relative 'app'

run Sinatra::Application
