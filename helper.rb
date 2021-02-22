def import_source_file(name)
  package = name[3].input[name[3].interval]
  p "import package:#{package}"
end

def debug(data, info)
  p 'debug'
end

def optional_node_exist?(node)
  node.elements.nil?
end

# TODO:monad
# ? 0,1
# if ?, then element will be nil
def optional_to_ast(node)
  if optional_node_exist? node
    # TODO: 'NotImplement'
    # for example, args may be exist or not exist
    []
  else
    begin
      node.to_ast
    rescue
      p 'error'
    end
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