require 'rspec'
require_relative '../Parser/parser'

def parse(src)
  Rc::Parser.parse(src).to_ast
end

def test_one_fun(src, fun_name, args = [])
  root = parse(src)
  expect(root.class).to eq(Rc::Root)
  expect(root.defines.size).to eq 1
  fun = root.defines[0]
  expect(fun.class).to eq(Rc::Function)
  expect(fun.name).to eq(fun_name)
  expect(fun.args).to eq(args)
  fun
end

describe 'Parser' do
  before do
    # Do nothing
  end

  after do
    # Do nothing
  end

  context 'def fun' do
    context 'empty fun body'
    it 'empty fun' do
      f1 = <<F
def foo()

end
F
      test_one_fun(f1, 'foo')
      f2 = <<F
      def foo

      end
F
      test_one_fun(f2, 'foo')
    end

    it 'fun has param' do
      f1 = <<F
      def foo(a, b)

      end
F
      test_one_fun(f1, 'foo', %w[a b])
    end

    context 'full fun body' do
      it '' do
        f1 = <<F
      def foo(a, b, c)
        a + b + c
      end
F
        f = test_one_fun(f1, 'foo', %w[a b c])
        expect(f.stmts.size).eql? 1
        expect(f.stmts[0].class).eql? Rc::Expr
      end
    end
  end

  def test_import(src, packages = [])
    root = parse(src)
    expect(root.class).to eq(Rc::Root)
    expect(root.defines).eql? []
    expect(root.packages).eql? packages
  end

  context 'import' do
    it 'import one succeeds' do
      i = <<import
    import set
import
      test_import(i, %w[set])
    end

    it 'import multi succeeds' do
      i = <<import
    import set
    import hash
    import json
import
      test_import(i, %w[set hash json])
    end
  end
end