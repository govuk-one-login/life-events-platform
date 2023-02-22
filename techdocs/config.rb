require 'govuk_tech_docs'
require 'middleman'
require_relative 'lib/inset_text_extension'
activate :relative_assets
activate :inset_text
set :relative_links, true

GovukTechDocs.configure(self)
