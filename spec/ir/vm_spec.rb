require 'rspec'
require 'set'
require './ir/vm/vm'
require './ir/ast/ast_node'
require './analysis/global_env'
require_relative '../parser_helper'
require_relative '../env_helper'

def get_vm_inst(src)
  ast = parse(src)
  env = Rc::Analysis::GlobalEnvVisitor.new.analysis(ast)
  to_vm_inst(ast, env)
end

def get_first_fun_inst(src)
  get_vm_inst(src)[1..]
end

include Rc::VM
include Rc::VM::Inst
# describe 'old VM inst' do
#   # todo:add id test
#   context 'assign' do
#     it 'normal expr' do
#       s = <<SRC
# def foo
#   a = 1 * 2
# end
# SRC
#       inst = get_first_fun_inst(s)
#       expect(inst).to eq [Push.new(1), Push.new(2), Mul.new, SetLocal.new(0), Return.new]
#     end
#   end
#
#   context 'fun' do
#     it 'succeed' do
#       s = <<SRC
# def add(a, b)
#   a + b
# end
# SRC
#       inst = get_first_fun_inst(s)
#       expect(inst).to eq [GetLocal.new(0), GetLocal.new(1), Add.new, Return.new]
#     end
#   end
#
#   context 'call' do
#     it 'normal' do
#       s = <<SRC
# def add(a, b)
#   a + b
# end
# def f
#   add(3, 2)
# end
# SRC
#       list = get_vm_inst(s)
#       expect(list[6..]).to eq [Push.new(3), Push.new(2), Call.new('add'), Return.new]
#     end
#   end
# end

def get_new_global_env(src)
  ast = parse(src)
  env = Rc::Analysis::GlobalEnvVisitor.new.analysis(ast)
  VMInstTranslator.new.tran(env)
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
      expect(f1.offset).to eq Rc::Define::UndefinedMethod
    end
  end
end