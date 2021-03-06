require './ast_node'

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
    begin
      node.to_ast
    rescue
      node
    end
  else
    # TODO: 'NotImplement'
    # for example, args may be exist or not exist
    []
  end                  
end

# + 1-n
def plus_to_ast(list)
  list.elements.map(&:to_ast)
end

# * 0-n
# if *, then element will be empty
def multi_to_ast(list)
  if list.elements.empty?
    []
  elsif list.elements.nil?
    raise 'Unknown error', list
  else
    plus_to_ast list
  end
end

def to_instance_node(ast)
  InstanceNode.new(ast.class,{:_val => ast})
end