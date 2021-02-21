require './log'

# TODO:valid check
# TODO:Err Check
# TODO:按照现有形式构造ast呢还是统一to_ast
class RootNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @packages = elements[0].elements
    @other = elements[1].elements
  end

  def eval(env = {})
    # import packages
    @packages.each { |p| p.eval(env) }
    # read statement
    @other.each do |s|
      case s
      in FunNode[fun_name, _, _]
        # TODO:first check valid, then add to env
        eval_log "fun:#{fun_name} has be added to env"
        env[fun_name] = s
      in ClassNode[class_name, _]
        eval_log "class:#{class_name} has be added to env"
        env[class_name] = s
      else
        # statement
        s.eval(env)
      end
    end

    FunCallNode.new("", "", ["main"], false)
    env["main"].eval(env.dup)
  end

  def inspect(indent = nil)
    (@packages.map{|p| p.inspect} +
      @other.map{|s| s.inspect}).join("\n")
  end
end

class PackageNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @package_name = elements[3].inspect
    $logger.debug "import package:#{@package_name}"
  end

  def import
    # TODO:parser import file, get env
    # multi main err
    # if error, then save the err msg
    {}
  end

  def eval(env = {})
    env.merge import
  end

  def inspect(indent = nil)
    "import \"#{@package_name}\""
  end
end

class FunNode < Treetop::Runtime::SyntaxNode
  attr_reader :args
  def initialize(input, interval, elements = nil)
    @name = elements[3].inspect
    if has_args?(elements)
      @args = []
    else
      @args = elements[5].args
    end
    @statements = elements[7]
    $logger.debug "implement fun:#{@name}"
  end

  # if no args, then will not construct FunArgsNode
  # so should judge by this way
  def has_args?(elements)
    elements[5].elements.nil?
  end

  def inspect(indent = nil)
    "fun:#{@name} args:(#{args.map(&:inspect).join(',')})"
  end

  def deconstruct
    [@name, @args, @statements]
  end

  def eval(env = {}, args = [])
    args.zip(@args)
    @statements.eval(env)
  end
end

class ClassNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @name = elements[3].inspect
    @define = elements[-2].elements
    $logger.debug "implement class:#{@name}"
  end

  def inspect(indent = nil)
    "class:#{@name}"
  end

  def deconstruct
    [@name, @define]
  end
end

class StatementNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @statement = elements[0]
  end

  def eval(env = {})
    eval_log "#{@statement.inspect}"
    @statement.eval(env)
  end

  def inspect(indent = nil)
    @s.inspect
  end
end

class VariantNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @id = elements[2].inspect
    @val_expr = elements[-1]
    $logger.debug "var:#{@id} val:#{@val_expr.inspect}"
  end

  def inspect(indent = nil)
    "var #{@id} = #{@val_expr.inspect}"
  end

  def eval(env = {})
    env[@id] = @val_expr.eval(env)
  end
end

class IfNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @if_condition = elements[2]
    @if_statements = elements[4]
    # TODO:5 6 maybe nil or empty
    all_elsif = elements[5].elements
    # [cond, statements]
    @elsif_list = all_elsif.map{|e| destruct_elsif(e)}
    @else_statements = elements[6].elements[2]
    $logger.debug "if node"
  end

  def destruct_elsif(elsif_node)
    [elsif_node.elements[2], elsif_node.elements[4]]
  end

  def inspect(indent = nil)
    'if'
  end

  def eval(env = {})
    if @if_condition.eval(env)
      @if_statements.eval(env)
    else
      # else if
      @elsif_list.each do |e|
        if e[0].eval(env)
          return e[1].eval(env)
        end
      end
      # TODO:return error
      # else
      @else_statements.eval(env)
    end
  end
end

class OpNode < Treetop::Runtime::SyntaxNode
  attr_accessor :op

  def initialize(input, interval, elements = nil)
    @op = input[interval]
  end

  def inspect(indent = nil)
    @op
  end

  def eval(env = {})
    @op
  end
end

class TermNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    # because of treetop's question
    # that can't set class before identifier
    begin
      @term = elements[0].elements[1]
      if @term.meta == 'id'
        @term = elements[0]
      end
    rescue
      @term = elements[0]
    end
  end

  def inspect(indent = nil)
    @term.inspect
  end

  def eval(env = {})
    @term.eval(env)
  end
end

class ExprNode < Treetop::Runtime::SyntaxNode
  attr_accessor :term_list

  def initialize(input, interval, elements = nil)
    @term_list = [elements[0]] + elements[-1].elements.map { |e| [e.elements[1], e.elements[3]] }.flatten
  end

  def inspect
    @term_list.map(&:inspect).join(' ')
  end

  def eval(env = {})
    # TODO:when fun call
    expr = @term_list.map do |term|
      term.eval(env)
    end.join(' ')
    Kernel.eval(expr)
  end
end

class FunCallNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil, by_treetop = true)
    if by_treetop
      @fun_name = elements[0].inspect
      @args = elements[1].args
      # @args = elements[1].elements
      $logger.debug "call:#{@fun_name}"
    else
      @fun_name, @args = elements
    end
  end

  def inspect(indent = nil)
    "#{@fun_name}(#{@args.map(&:inspect).join(',')})"
  end

  # TODO:env merge
  def eval(env = {}, args = nil)
    fun = env[@fun_name]
    if fun.class == FunNode
      # fun_args => call_args
      # merge env and args
      eval"call:#{@fun_name}"
      fun.eval(env.dup, @args)
    else
      # TODO:exception class
      $logger.error "error env::#{env}"
      raise 'Exception'
    end
  end

  # used by distinguish between call and identifier
  def meta
    'call'
  end
end

class IdentifierNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @name = input[interval]
    # $logger.debug "id:#{@name}"
  end

  def inspect(indent = nil)
    @name
  end

  def eval(env = {})
    # TODO:if name not exist, throw exception
    env[@name]
  end
end

class BoolConstant < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @val = input[interval]
  end

  def inspect(indent = nil)
    @val
  end

  def eval(env = {})
    Kernel.eval(@val)
  end
end

class NumberConstant < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @val = input[interval]
  end

  def inspect(indent = nil)
    @val
  end

  def eval(env = {})
    Kernel.eval(@val)
  end
end

class StringConstant < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @val = input[interval]
  end

  def inspect(indent = nil)
    @val
  end

  def eval(env = {})
    @val
  end
end

class BoolConstant < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @val = input[interval]
  end

  def inspect(indent = nil)
    @val
  end

  def eval(env = {})
    Kernel.eval(@val)
  end
end

class StatementsNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @elements = elements
  end

  # TODO:if elements nil
  def inspect(indent = nil)
    @elements.map(&:inspect)
  end

  def eval(env = {})
    @elements.each {|s| s.eval(env)}
  end
end

class FunArgsNode < Treetop::Runtime::SyntaxNode
  attr_reader :args
  # TODO:repeat var name check
  def initialize(input, interval, elements = nil)
    args = elements[1].elements
    if args.nil?
      @args = []
    else
      @args = [args[1].inspect] + args[3].elements.map{|e| e.elements[2].inspect}
    end
  end
end

class CallArgsNode < Treetop::Runtime::SyntaxNode
  attr_reader :args
  def initialize(input, interval, elements = nil)
    if elements[1].elements.nil?
      @args = []
    else
      # args = elements[1].elements
      # @args = [args[2]] + args[3].elements.map { |e| e.elements[3] }
      args = elements[1].elements
      @args = [args[1]] + args[2].elements.map {|e| e.elements[3]}
    end
  end
end