require './ir/tac/tac'
require './ir/ssa/ssa'
require './ir/cfg'
require './analysis/call_graph'
require './analysis/global_env'
require './parser/parser'
require './ir/vm/vm'

module Rc
  class Compiler
    def parse(input)
      input = Parser.parse(input)
      input.to_ast
    end

    def compile(input)
      ast = parse(input)
      g_env = Analysis::GlobalEnvVisitor.new.analysis(ast)
      compile_to_vm(ast, g_env)
    end

    def compile_to_vm(ast, global_env)
      puts 'To VM Inst'
      global_table = VM.to_vm_inst(global_env)
      root = '../RCVM/cmake-build-debug/'
      VM.generate_all(global_table, root)
    end

    def compile_to_native(ast, global_env)
      puts 'To Tac'
      tac = TAC.to_tac(ast, env)
      puts tac
      puts 'To CFG'
      tac.process { |f| CFG.to_cfg(f.tac_list) }
      puts tac
      puts 'Reorder branches'
      tac.process { |f| CFG.reorder_branches(f) }
      puts tac
      # cfg_tac = CFG.to_cfg(tac.tac_list)
      # puts cfg_tac
      # cfg_tac.fun_list[0].to_dot("/home/homura/Code/Rc-lang/test.png")
      # puts 'To roads'
      # roads = CFG.search_all_branches(cfg)
      # puts roads
    end
  end
end