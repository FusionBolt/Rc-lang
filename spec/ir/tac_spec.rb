require 'rspec'
require 'set'
require './ir/tac/tac'
require_relative 'ir_helper'

include Rc::TAC
describe 'tac' do
  context 'assign' do
    it 'succeed' do
      s = <<SRC
def f1
  a = 1
  b = 2
  c = a * b
end
SRC
      # todo:can be refactor
      tac = get_tac(s)
      list = tac.first_fun_tac_list
      expect(list[1]).to eq Assign.new(Name.new('a'), Number.new(1))
      expect(list[2]).to eq Assign.new(Name.new('b'), Number.new(2))
      expect(list[3]).to eq Quad.new('*', TempName.new('0'), Name.new('a'), Name.new('b'))
      expect(list[4]).to eq Assign.new(Name.new('c'), TempName.new('0'))
    end
  end

  context 'constructor' do
    it 'succeed' do
      s = <<NEW
class Foo
end
def f
  Foo.new()
end
NEW
      tac = get_tac(s)
      # todo:fun_list set a class, override['f name']
      alloc = tac.fun_list[1][1]
      call = tac.fun_list[1][2]
      expect(alloc.is_a? Rc::TAC::Alloc).to eq true
      expect(alloc.type).to eq 'Foo'
      expect(call.is_a? Rc::TAC::Call).to eq true
      expect(call.args.size).to eq 1
    end
  end

  context 'call' do
    it 'succeed' do
      s = <<CALL
def f1(a, b, c)
  a + b + c
end
def f
  f1(1, 2, 3)
end
CALL
      tac = get_tac(s)
      f = tac.fun_list[1]
      call_f1 = f[1]
      expect(call_f1.is_a? Rc::TAC::Call)
      expect(call_f1.target).to eq Name.new('f1')
      expect(call_f1.args).to eq [Rc::TAC::Number.new(1), Rc::TAC::Number.new(2), Rc::TAC::Number.new(3)]
    end
  end

  context 'string' do
    it 'assign' do
      src = <<SRC
def f1
  a = 'str'
end
SRC
      tac = get_tac(src)
      expect(tac.const_table).to eq ['str']
      assign = tac.first_fun_tac_list[1]
      expect(assign.is_a? Rc::TAC::Quad)
      expect(assign.lhs).to eq Memory.new(0)
    end

    it 'args' do
      src = <<SRC
def f1
  puts('The World!')
end
SRC
      tac = get_tac(src)
      expect(tac.const_table).to eq ['The World!']
      call = tac.first_fun_tac_list[1]
      expect(call.args).to eq [Memory.new(0)]
    end
  end

  context 'fun' do
    it 'empty fun' do
      s = <<SRC
def f1
  
end
SRC
      tac = get_tac(s)
      list = tac.first_fun_tac_list
      expect(tac.sym_table.has_key? 'f1')
      expect(list[0]).to eq Rc::TAC::Label.new('f1')
      expect(list[1]).to eq Rc::TAC::Return.new(EmptyValue.new)
    end

    # todo:this case is fail, when last stmt is a expr,
#     it 'direct return value' do
#       s = <<SRC
# def f1
#  1
# end
# SRC
#       list = get_first_fun_tac_list(s)
#       expect(list[1]).to eq Return.new(Number.new(1))
#     end
    # todo:test last is if and while
    it 'return compute result' do
      s = <<SRC
def add(a, b)
  a + b
end
SRC
      list = get_first_fun_tac_list(s)
      expect(list[1]).to eq Quad.new('+', TempName.new('0'), Name.new('a'), Name.new('b'))
      expect(list[2]).to eq Rc::TAC::Return.new(TempName.new('0'))
    end
  end

  context 'compose' do
    it 'assign call result' do
      s = <<SRC
def f
  v = f1(1, 2, 3)
end

def f1(a, b, c)
  a + b + c
end
SRC
      tac = get_tac(s)
      list = tac.first_fun_tac_list
      call_result = TempName.new('0')
      expect(list[1]).to eq Rc::TAC::Call.new(call_result, Name.new('f1'), [Number.new(1), Number.new(2), Number.new(3)])
      expect(list[2]).to eq Assign.new(Name.new('v'), call_result)
      expect(list[3].class).to eq Rc::TAC::Return
    end
  end
end