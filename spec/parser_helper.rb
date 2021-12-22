require 'rspec'
require './parser/parser'
require './ir/ast/ast_node'

module RcTestHelpers
  include Rc::AST
  def parse(src)
    Rc::Parser.parse(src).to_ast
  end

  def parse_demo(name)
    path = File.join(File.dirname(__FILE__), '..', 'demo', "#{name}.rc")
    src = File.open(path).read
    parse(src)
  end

  def test_root_single_define(src)
    root = parse(src)
    expect(root.class).to eq(Root)
    expect(root.defines.size).to eq 1
    root.defines[0]
  end

  def test_import(src, packages = [])
    root = parse(src)
    expect(root.class).to eq Root
    expect(root.defines).to eq []
    expect(root.packages.map { |p| p.name }).to eq packages
    root.packages
  end
  
  def test_one_fun_sign(fun, name, args)
    expect(fun.name).to eq(name)
    expect(fun.args).to eq(args)
  end

  def test_one_fun(src, name, args = [])
    fun = test_root_single_define(src)
    test_one_ast_fun(fun, name, args)
  end

  def test_one_ast_fun(fun, name, args = [])
    expect(fun.class).to eq(Function)
    test_one_fun_sign(fun, name, args)
    fun
  end

  # def test_one_ast_fun_body
  # def test_multi_fun
  # def test_multi_ast_fun

  def test_one_empty_fun(src, name, args = [])
    fun = test_root_single_define(src)
    test_one_fun_sign(fun, name, args)
    expect(fun.empty?).to be true
    fun
  end

  def test_lambda(src, fun_name, args = [])
    lambda_stmt = test_root_single_define(src)
    lambda_obj_name = lambda_stmt.stmt.var_obj.name
    f = lambda_stmt.stmt.expr.term_list[0]
    expect(lambda_obj_name).to eq fun_name
    test_one_fun_sign(f, 'lambda', args)
    f
  end

  def test_class(src, classes = [])
    root = parse(src)
    expect(root.packages).to eq []
    expect(root.defines.size).to eq classes.size
    expect(root.defines.detect { |d| d.class != ClassDefine }).to eq nil
    expect(root.defines.map { |d| d.name }).to eq classes
    if classes.size == 1
      root.defines[0]
    else
      root.defines
    end
  end

  def test_expr(src, str)
    root = parse(src)
    expect(root.to_s).to eq str
  end
end