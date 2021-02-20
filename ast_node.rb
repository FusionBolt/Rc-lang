class RootNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @packages = elements[0].elements
    @other = elements[1].elements
  end

  def eval(env = {})
    # import packages
    @packages.each {|p| p.eval(env)}
    # read statement
    @other.each do |s|
      case s
      in FunNode["main", _, main_fun]
        main_fun.eval(env)
      in FunNode[fun_name, _, _]
        if fun_name == "main"
          s.eval(env)
        else
          env[fun_name] = s
        end
      in ClassNode[class_name, _]
        env[class_name] = s
      else
        # statement
        statement = s.elements[0]
        statement.eval(env)
      end
    end
  end
end

class PackageNode < Treetop::Runtime::SyntaxNode
  # TODO:如果不需要创建package类就
  # &{|seq| import_source_file seq; true} <PackageNode>
  def initialize(input, interval, elements = nil)
    @package_name = elements[3].inspect
    p "import package:#{@package_name}"
  end

  def import
    # TODO:parser import file, get env
    {}
  end

  def eval(env = {})
    env.merge import
  end
end

class FunNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @name = elements[3].inspect
    @args = elements[5].elements
    @statements = elements[7].elements
    fun = input[interval]
    p "implement fun:#{fun}"
  end

  def deconstruct
    [@name, @args, @statements]
  end

  def eval(env = {})

  end
end

class ClassNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @class_name = elements[4].inspect
    @class_define = elements[-2].elements
    c = input[interval]
    p "implement class:#{c}"
  end

  def deconstruct
    [@class_name, @class_define]
  end
end

class VariantNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @id = elements[2].inspect
    @val_expr = elements[-1]
    p "var:#{@id} val:#{@val_expr.inspect}"
  end

  def eval(env = {})
    env[@id] = @val_expr.eval(env)
  end
end

class IfNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @if_condition = elements[2].inspect
    @if_statement = elements[4].elements
    # TODO:5 6 maybe nil or empty
    @elsif = elements[5].elements
    @else = elements[6].elements
    p input[interval]
  end
end

class OpNode < Treetop::Runtime::SyntaxNode
  attr_accessor :op
  def initialize(input, interval, elements = nil)
    @op = input[interval]
    #p "op:#{@op}"
  end

  def inspect(indent = nil)
    @op
  end
end

class TermNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @term = input[interval]
    #p "term:#{@term}"
  end

  def inspect(indent = nil)
    @term
  end
end

class ExprNode < Treetop::Runtime::SyntaxNode
  attr_accessor :term_list
  def initialize(input, interval, elements = nil)
    @term_list = [elements[0]] + elements[1].elements.map{|e| [e.elements[1], e.elements[3]]}.flatten
    # TODO: 1+2 +3 will error
    #p "expr:#{inspect}"
  end

  def inspect
    @term_list.map(&:inspect).join(' ')
  end

  def eval(env = {})
    # TODO:when fun call
    expr = @term_list.map do |term|
      if term.is_a? IdentifierNode
        term.eval(env)
      else
        term.inspect
      end
    end.join(' ')
    Kernel.eval(expr)
  end
end

class FunCallNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @fun_name = elements[0].inspect
    @args = elements[1]
    p "call:#{@fun_name}"
  end
end

class IdentifierNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @name = input[interval]
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
end

class NumberConstant < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @val = input[interval]
  end
end

class StringConstant < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @val = input[interval]
  end
end

class BoolConstant < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    @val = input[interval]
  end
end