require 'rspec'
require 'set'
require_relative '../../analysis/global_env'
require_relative '../parser_helper'

describe Rc::Analysis::GlobalEnvVisitor do
  before do
    # Do nothing
  end

  after do
    # Do nothing
  end

  context 'GlobalEnvVisitor' do
    it 'succeeds' do
      ast = parse_demo('call_graph')
      env = Rc::Analysis::GlobalEnvVisitor.new.analysis(ast)
      expect(env.keys).to eq %w[f1 f2 f3 main]
    end
  end

end