require 'treetop'
require './ast_node'

class Parser
  Treetop.load('./syntax.treetop')
  @@parser = RcParser.new

  def self.parse(data)
    begin
      tree = @@parser.parse data
    rescue => e
      p e
    end

    if tree.nil?
      p @@parser.failure_reason
      p @@parser.failure_line
      p @@parser.failure_column
      # TODO:err recover and more err info
      return "Parse error at offset: #{@@parser.index}"
    end
    $logger.info '----------------Parse end----------------'
    tree
  end
end