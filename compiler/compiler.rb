require_relative '../ir/tac/tac'
require_relative '../ir/ssa/ssa'
require_relative '../ir/cfg'
require_relative '../analysis/call_graph'
require_relative '../analysis/global_env'
require './parser/parser'

module Rc
  class Compiler
    def parse(input)
      Parser.parse(input).to_ast
    end

    def compile(input)
      ast = parse(input)
      env = Analysis::GlobalEnvVisitor.new.analysis(ast)
      main = env['main']
      puts 'To Tac'
      tac = Tac.to_tac(main)
      puts tac
      puts 'To CFG'
      cfg = CFG.to_cfg(tac)
      puts cfg
      # puts 'To SSA'
      # ssa = SSA.to_ssa(tac)
      # puts ssa
    end
  end
end