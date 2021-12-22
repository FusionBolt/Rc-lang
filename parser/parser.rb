require 'treetop'
require_relative '../ir/ast/ast_node'
require './lib/error'

module Rc
  class Parser
    # TODO:file path question
    Treetop.load('./parser/syntax.treetop')
    @@parser = RcParser.new

    def self.parse(data)
      begin
        tree = @@parser.parse data
      rescue => e
        p e
      end

      if tree.nil?
        $logger.error "#{@@parser.failure_reason}, #{@@parser.failure_line}, #{@@parser.failure_column}"
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
