class PackageNode < Treetop::Runtime::SyntaxNode
  # TODO:如果不需要创建package类就
  # &{|seq| import_source_file seq; true} <PackageNode>
  def initialize(input, interval, elements = nil)
    package = input[elements[3].interval]
    p "import package:#{package}"
  end

  def import

  end
end

class FunNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    fun = input[elements[3].interval]
    p "implement fun:#{fun}"
  end
end

class ClassNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    c = input[elements[3].interval]
    p "implement class:#{c}"
  end
end

class VariantNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    variant = input[elements[2].interval]
    p "variant:#{variant}"
  end
end

class IfNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    p "new if"
  end
end

class ExprNode < Treetop::Runtime::SyntaxNode
  def initialize(input, interval, elements = nil)
    # p "new expr, term start:#{input[elements[0].interval]}"
  end
end