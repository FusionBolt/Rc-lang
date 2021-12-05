require 'rspec'
require_relative '../parser/parser'
require_relative 'parser_helper'

describe Rc::Parser do
  before do
    # Do nothing
  end

  after do
    # Do nothing
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

        test_one_empty_fun(f2, 'foo')
      end

      it 'fun has param' do
        f1 = <<F
      def foo(a, b)

      end
F
        test_one_empty_fun(f1, 'foo', %w[a b])
      end
    end
    context 'full fun body' do
      it 'has expr' do
        f1 = <<F
      def foo(a, b, c)
        a + b + c
      end
F
        f = test_one_fun(f1, 'foo', %w[a b c])
        expect(f.stmts.size).to eq 1
        expect(f.stmts[0].stmt.class).to eq Rc::Expr
      end
    end

    context 'lambda' do
      it 'empty' do
        l = <<LAMBDA
    f = ->(){}
LAMBDA
        test_lambda(l, 'f', [])
      end

      it 'has param' do
        l = <<LAMBDA
    l = ->(x, y){}
LAMBDA
        test_lambda(l, 'l', %w[x y])
      end

      it 'has body' do
        l = <<LAMBDA
    l = ->(x, y){ print("lambda") }
LAMBDA
        test_lambda(l, 'l', %w[x y])
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

    context 'member fun' do
      it 'init' do
        c = <<CLASS
    class Set

      def init

      end
    end
CLASS
        c = test_class(c, %w[Set])
        expect(c.define.size).to eq 1
        init_fun = c.define[0]
        expect(init_fun.class).to eq Rc::Function
        expect(init_fun.name).to eq 'init'
        expect(init_fun.stmts.empty?).to be true
      end

      it 'multi mem fun' do
        c = <<CLASS
      class Foo
        def init(a, b)

        end

        def f1(a, b, c)

        end

        def f2(m, n)

        end
      end
CLASS
        c = test_class(c, ['Foo'])
        expect(c.define.size).to be 3
        test_one_fun_sign(c.define[0], 'init', %w[a b])
        test_one_fun_sign(c.define[1], 'f1', %w[a b c])
        test_one_fun_sign(c.define[2], 'f2', %w[m n])
      end

      context 'inherit' do
        it 'inherit a empty class' do
          c = <<CLASS
    class Object

    end

    class Foo < Object

    end
CLASS
          cs = test_class(c, %w[Object Foo])
          expect(cs[1].parent).to eq cs[0].name
        end
      end

      context 'member var' do
        it 'no default val' do
          c = <<CLASS
    class Foo
      var a
    end
CLASS
          c = test_class(c, %w[Foo])
          expect(c.define.size).to eq 1
          expect(c.define[0].class).to be Rc::ClassMemberVar
        end
        it 'has default val' do
          c = <<CLASS
    class Foo
      var a = 1
    end
CLASS
          c = test_class(c, %w[Foo])
          expect(c.define.size).to eq 1
          v = c.define[0]
          expect(v.class).to be Rc::ClassMemberVar
        end
      end
    end
  end
end