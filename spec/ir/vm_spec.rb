require 'rspec'
require 'set'
require './ir/vm/vm'
require './ir/ast/ast_node'
require './analysis/global_env'
require_relative '../parser_helper'
require_relative '../env_helper'

include Rc::VM
include Rc::VM::Inst

def get_new_global_env(src)
  ast = parse(src)
  env = Rc::Analysis::GlobalEnvVisitor.new.analysis(ast)
  VMInstTranslator.new.translate(env)
end

describe 'VM Inst' do
  context 'global method' do
    it 'successful' do
      s = <<SRC
def f1(a, b)
  c = a + b
  c
end

def f2
end
SRC
      env = get_new_global_env(s)
      f1 = get_kernel_methods_info(env)['f1']
      expect(f1.define.is_a? Array)
      expect(f1.env.keys).to eq %w[a b c]
      expect(f1.args).to eq %w[a b]
    end
  end
end