require 'rspec'
require 'set'
require './ir/tac/tac'
require_relative 'ir_helper'

describe 'tac' do
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
      expect(call_f1.is_a? Rc::TAC::Call).to eq true
      expect(call_f1.target).to eq 'f1'
      expect(call_f1.args).to eql [Rc::TAC::Number.new(1), Rc::TAC::Number.new(2), Rc::TAC::Number.new(3)]
    end
  end
end