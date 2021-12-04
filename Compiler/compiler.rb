require_relative 'code_gen'
require_relative 'ssa'

module Rc
  class Compiler
    def parse(input)
      Parser.parse(input).to_ast
    end

    def compile(input)
      ast = parse(input)
    end
  end
end