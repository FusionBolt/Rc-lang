require_relative '../ir/ssa'
require './parser/parser'

module Rc
  class Compiler
    def parse(input)
      Parser.parse(input).to_ast
    end

    def compile(input)
      ast = parse(input)
      env = Analysis::GlobalEnvVisitor.new.analysis(ast)
      Rc.fun_to_ssa(env['main'])
    end
  end
end