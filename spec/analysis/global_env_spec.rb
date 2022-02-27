require 'rspec'
require 'set'
require './analysis/global_env'
require_relative '../parser_helper'
require_relative '../env_helper'
require './lib/env'

def get_global_env(s)
  ast = parse(s)
  Rc::Analysis::GlobalEnvVisitor.new.analysis(ast)
end


def get_kernel_methods_from_src(src)
  env = get_global_env(src)
  get_kernel_methods_info(env)
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
      # expect(env.define_env.keys).to eq %w[f1 f2 f3 main]
      # expect(env.const_table.empty?).to eq true
      expect(get_kernel_methods_info(env).keys).to eq %w[f1 f2 f3 main]
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
      expect(get_kernel_methods_info(env).keys).to eq %w[foo]
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
      # ast = parse(src)
      # env = Rc::Analysis::GlobalEnvVisitor.new.analysis(ast)
      methods = get_kernel_methods_from_src(src)
      expect(methods.has_key? 'foo').to eq true
      e = methods['foo'].env
      expect(e['a']).to eq Rc::EnvItemInfo.new(0, '')
      expect(e['b']).to eq Rc::EnvItemInfo.new(1, '')
    end
  end

  context 'kernel fun' do
    it 'multi fun' do
      s = <<SRC
def f1
end
def f2
end
SRC
      env = get_global_env(s)
      # expect(env.fun_env.has_key? 'f1')
      # expect(env.fun_env.has_key? 'f2')
      expect(get_kernel_methods_info(env).keys).to eq %w[f1 f2]
    end
  end

  context 'class' do
    def check_method(env, klass, method_list)
      expect(env.class_table.has_key?(klass)).to eq true
      klass_table = env.class_table[klass]
      expect(klass_table.instance_methods.keys).to eq method_list
    end


    it 'single method' do
      s = <<SRC
def main
end
SRC
      env = get_global_env(s)
      check_method(env, Rc::Define::GlobalObject, %w[main])
    end

    it 'class method' do
      s = <<SRC
class Foo
  def f1
  end
end
SRC
      env = get_global_env(s)
      check_method(env, 'Foo', %w[f1])
    end

    it 'class and global method' do
      s = <<SRC
class Foo
  def f1
  end
end
def main
end
SRC
      env = get_global_env(s)
      check_method(env, Rc::Define::GlobalObject, %w[main])
      check_method(env, 'Foo', %w[f1])
    end

    context 'inherit' do
      it 'single' do
        s = <<SRC
class Parent
end

class Foo < Parent
end
SRC
        env = get_global_env(s)
        expect(Set.new(env.class_table.keys)).to eq Set.new(%w[Kernel Parent Foo])
        expect(env.class_table['Foo'].parent).to eq "Parent"
      end
    end
  end
end