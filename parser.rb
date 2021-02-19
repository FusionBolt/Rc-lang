require 'treetop'
require './ast_node'

class Parser
  # TODO:file path
  Treetop.load('./syntax.treetop')
  @@parser = LParser.new

  def self.parse(data)
    tree = @@parser.parse data

    if tree.nil?
      p @@parser.failure_reason
      p @@parser.failure_line
      p @@parser.failure_column
      # TODO:err recover and more err info
      return "Parse error at offset: #{@@parser.index}"
    end
    # p @@parser.failure_reason
    # p @@parser.failure_line
    # p @@parser.failure_column
    self.clean_tree tree
    tree
  end

  def self.clean_tree(root_node)
    return if(root_node.elements.nil?)
    root_node.elements.delete_if {|node| node.class.name == "Treetop::Runtime::SyntaxNode" }
    root_node.elements.each {|node| self.clean_tree(node) }
  end
end