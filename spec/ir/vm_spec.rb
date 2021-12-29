require 'rspec'
require 'set'
require './ir/vm/vm'
require_relative '../parser_helper'

def get_vm_inst(src)
  ast = parse(src)
  env = Rc::Analysis::GlobalEnvVisitor.new.analysis(ast)
  to_vm_inst(ast, env)
end

include Rc::VM
describe 'VM inst' do
  context 'quad' do
    context 'assign' do
      it 'normal expr' do
        s = <<SRC
def foo
  a = 1 * 2
end
SRC
        inst = get_vm_inst(s)
        expect(inst).to eq [Rc::VM::Push.new(1), Push.new(2), Mul.new, SetLocal.new(0)]
      end
    end
  end

  context 'call' do
#     it 'normal' do
#       s = <<SRC
# def add(a, b)
#   a + b
# end
# def f
#   add(1, 2)
# end
# SRC
#       list = get_vm_inst(s)
#       expect(list).to eq [Push.new(1), Push.new(2), Call.new('add')]
#     end
  end

  context 'return' do

  end
end