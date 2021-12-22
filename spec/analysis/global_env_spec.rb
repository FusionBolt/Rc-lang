require 'rspec'
require 'set'
require './analysis/global_env'
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
      env, sym_table = Rc::Analysis::GlobalEnvVisitor.new.analysis(ast)
      expect(env.keys).to eq %w[f1 f2 f3 main]
      expect(sym_table.empty?).to eq true
    end
  end

  context 'String' do
    it 'succeeds' do
      src = <<STR_TABLE
def foo
  a = "str1"
  b = "str2"
end
STR_TABLE
      ast = parse(src)
      env, sym_table = Rc::Analysis::GlobalEnvVisitor.new.analysis(ast)
      expect(env.keys).to eq %w[foo]
      expect(sym_table).to eq Set['str1', 'str2']
    end
  end
end