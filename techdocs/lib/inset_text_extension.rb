class InsetTextExtension < Middleman::Extension
  def initialize(app, options_hash = {}, &block)
    super
  end

  helpers do
    def inset_text(text)
      <<~EOS
        <div class="govuk-inset-text">
          #{text}
        </div>
      EOS
    end
  end
end

Middleman::Extensions.register(:inset_text, InsetTextExtension)
