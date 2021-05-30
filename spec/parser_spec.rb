require 'rspec'
require_relative '../Parser/parser'

def parse(src)
  begin
    Rc::Parser.parse(src).to_ast
  rescue
    nil
  end
end

def test_class(src, classes = [])
  root = parse(src)
  expect(root.packages).to eq []
  expect(root.defines.size).to eq classes.size
  expect(root.defines.detect { |d| d.class != Rc::ClassDefine }).to eq nil
  expect(root.defines.map { |d| d.name }).to eq classes
  if classes.size == 1
    root.defines[0]
  else
    root.defines
  end
end

describe Rc::Parser do
  before do
    # Do nothing
  end

  after do
    # Do nothing
  end

  def test_import(src, packages = [])
    root = parse(src)
    expect(root.class).to eq Rc::Root
    expect(root.defines).to eq []
    expect(root.packages.map { |p| p.name }).to eq packages
    root.packages
  end

  context 'import' do
    it 'import one succeeds' do
      i = <<IMPORT
    import set
IMPORT
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

  context 'def fun' do
    context 'empty fun body' do
      it 'empty fun' do
        f1 = <<FUN
def foo()

end
FUN
        test_one_fun(f1, 'foo')
        f2 = <<FUN
      def foo

      end
FUN
        test_one_fun(f2, 'foo')
      end

      it 'fun has param' do
        f1 = <<F
      def foo(a, b)

      end
F
        test_one_fun(f1, 'foo', %w[a b])
      end
    end
    context 'full fun body' do
      it '' do
        f1 = <<F
      def foo(a, b, c)
        a + b + c
      end
F
        f = test_one_fun(f1, 'foo', %w[a b c])
        expect(f.stmts.size).to eq 1
        expect(f.stmts[0].class).to eq Rc::Expr
      end
    end
  end

  context 'class' do
    it 'empty class' do
      c = <<CLASS
    class Set
      
    end
CLASS
      test_class(c, %w[Set])
    end

    it 'has init' do
      c = <<CLASS
    class Set

      def init

      end
    end
CLASS
      c = test_class(c, %w[Set])
      expect(c.define.size).to eq 1
      expect(c.define[0].class).to eq Rc::Function
      expect(c.define[0].name).to eq 'init'
    end

    it 'inherit' do
      c = <<CLASS
    class Object

    end

    class Foo < Object

    end
CLASS
      test_class(c, %w[Object Foo])
    end
  end
end