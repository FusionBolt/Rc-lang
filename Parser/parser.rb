require 'treetop'
require_relative '../AST/ast_node'
require './Lib/error'

module Rc
  class Parser
    # TODO:file path question
    Treetop.load('./Parser/syntax.treetop')
    @@parser = RcParser.new

    def self.parse(data)
      begin
        tree = @@parser.parse data
      rescue => e
        p e
      end

      if tree.nil?
        p @@parser.failure_reason, @@parser.failure_line, @@parser.failure_column
        raise ParserError.new(
          @@parser.failure_reason,
          @@parser.failure_line,
          @@parser.failure_column
        )
        # TODO:err recover and more err info
      end
      $logger.info '----------------Parse end----------------'
      tree
    end
  end
end
