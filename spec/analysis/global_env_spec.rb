require 'rspec'
require 'set'
require './analysis/global_env'
require_relative '../parser_helper'
require './lib/env'

def get_global_env(s)
  ast = parse(s)
  Rc::Analysis::GlobalEnvVisitor.new.analysis(ast)
end

describe Rc::Analysis::GlobalEnvVisitor do
  before do
  end

  after do
    # Do nothing
  end

  context 'GlobalEnvVisitor' do
    it 'succeeds' do
      ast = parse_demo('call_graph')
      env = Rc::Analysis::GlobalEnvVisitor.new.analysis(ast)
      expect(env.define_env.keys).to eq %w[f1 f2 f3 main]
      expect(env.const_table.empty?).to eq true
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
      env = Rc::Analysis::GlobalEnvVisitor.new.analysis(ast)
      expect(env.define_env.keys).to eq %w[foo]
      expect(env.const_table).to eq Set['str1', 'str2']
    end
  end

  context 'local var number' do
    it 'succeed' do
      src = <<STR_TABLE
def foo
  a = 1
  b = 2
end
STR_TABLE
      @ast = parse(src)
      env = Rc::Analysis::GlobalEnvVisitor.new.analysis(@ast)
      expect(env.fun_env.has_key? 'foo').to eq true
      e = env.fun_env['foo']
      expect(e['a']).to eq Rc::EnvItemInfo.new(0, '')
      expect(e['b']).to eq Rc::EnvItemInfo.new(1, '')
    end
  end

  context 'fun' do
    it 'multi fun' do
      s = <<SRC
def f1
end
def f2
end
SRC
      env = get_global_env(s)
      expect(env.fun_env.has_key? 'f1').to eq true
      expect(env.fun_env.has_key? 'f2').to eq true
    end
  end
end