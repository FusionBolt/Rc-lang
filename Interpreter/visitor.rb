require_relative 'evaluate'
# TODO:传递参数
# 对象的问题
# CallStack
# about class and instance

def under_score_class_name(obj)
  underscore(obj.class.to_s)
end

def underscore(str)
  str.gsub(/::/, '/').
    gsub(/([A-Z]+)([A-Z][a-z])/, '\1_\2').
    gsub(/([a-z\d])([A-Z])/, '\1_\2').
    tr("-", "_").
    downcase
end

class Visitor
  def initialize
    @env = Env.new
    @evaluator = Evaluate.new(self, @env)
  end

  def visit(node)
    # TODO:method missing process
    method("on_#{under_score_class_name(node)}")[node]
  end

  def main(argv = [])
    if @env.has_key? 'main'
      $logger.info '---------------- start main ----------------'
      # TODO:change
      visit(Expression.new([FunCall.new('main')]))
    else
      raise 'MainNotFound'
    end
    p @env
  end

  # TODO:control save env
  def run_fun(fun, args)
    if args.length != fun.args.length
      raise 'ArgsLengthNotMatch'
    end
    # TODO:error, replace by scope
    @env.enter_fun(fun.args.zip(args.map{|arg|@evaluator.evaluate(arg)}).to_h)
    return_val = visit(fun.stmts)
    @env.exit_fun
    return_val
  end

  def on_nil_class(node)
    raise 'OnNilClass'
  end

  def on_root(node)
    # TODO:import
    node.packages.each {|n| visit(n)}
    node.other.each {|n| visit(n)}
  end

  def on_package(node)
    node.name
  end
  # TODO:preprocessing?
  def on_function(node)
    @env[node.name] = node
  end

  def on_class_define(node)
    init_fun = node.init
    if init_fun.nil?
      node.generate_init_fun
    else
      # some var may not be initialized state
      # look up which var not be initialized
      # TODO:not implement
      # init_fun.stmts.append()
    end
    @env[node.name] = node
  end

  def on_expression(node)
    @evaluator.evaluate(node)
  end

  # TODO:need a statement visitor?
  def on_statements(node)
    node.stmts.each do |n|
      return_val = visit(n)
      # TODO:refactor
      return return_val if n.statement.class == Return or n == node.stmts[-1]
    end
  end

  def on_statement(node)
    return if node.statement == []
    visit(node.statement)
  end

  def on_variant(node)
    @env[node.name] = @evaluator.evaluate(node.expr)
  end

  def on_if(node)
    # TODO:test
  end

  def on_assign(node)
    # TODO:成员变量赋值怎么办
    # TODO:检查var是否存在
    # TODO:未完全实现
    # TODO:成员变量赋值出错
    @env[node.var_obj.name] = @evaluator.evaluate(node.expr)
  end

  def on_return(node)
    @evaluator.evaluate(node.expr)
  end

  def on_debug_statement(node)
    p node.info
  end

  def on_break_point(node)
    # TODO:stop and get input
    # TODO:need a repl that can get val from env
    # TODO:a GUI for displaying debugging information
  end
end