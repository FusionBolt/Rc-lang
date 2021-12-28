require 'rspec'
require 'set'
require './ir/rcvm/vm'
require './ir/tac/tac'
require_relative 'ir_helper'

def translate_tac(tac)
  Rc::VM::RCVMInstTranslator.new.visit(tac)
end

def get_vm_inst(src)
  tac = get_tac(src)
  Rc::VM.to_vm_inst(tac)
end

# include Rc::VM
#
# describe 'VM inst' do
#   context 'quad' do
#     context 'assign' do
#       it 'normal expr' do
#         s = <<SRC
# def f
#   a = 1 * 2
# end
# SRC
#         list = get_vm_inst(s)
#         expect(list[1..4]).to eq [Rc::VM::Push.new(1), Push.new(2), Mul.new, SetLocal.new(0)]
#       end
#     end
#   end
#
#   context 'call' do
# #     it 'normal' do
# #       s = <<SRC
# # def add(a, b)
# #   a + b
# # end
# # def f
# #   add(1, 2)
# # end
# # SRC
# #       list = get_vm_inst(s)
# #       expect(list).to eq [Push.new(1), Push.new(2), Call.new('add')]
# #     end
#   end
#
#   context 'return' do
#
#   end
# end