require './AST/ast_node'
require './Lib/error'
require './Lib/types'
require './Lib/hack'

def debug(data, info = '')
  1 + 1
end

def optional_node_exist?(node)
  not node.elements.nil?
end

# TODO:monad
# ? 0,1
# if ?, then element will be nil
def optional_to_ast(node)
  if optional_node_exist? node
    node.try(&:to_ast)
  else
    Rc::Empty.new
  end
end

def get_args(node)
  val = optional_to_ast(node)
  if val.class == Rc::Empty
    []
  else
    val
  end
end

# + 1-n
def plus_to_ast(list)
  list.elements.map(&:to_ast)
end

# * 0-n
# if *, then element will be empty
# TODO:block given?
def multi_to_ast(list)
  if list.elements.empty?
    []
  elsif list.elements.nil?
    raise Rc::UnknownError.new(list)
  else
    plus_to_ast list
  end
end

def to_instance_node(ast)
  Rc::Instance.new(ast.class, { :_val => ast})
end