require_relative '../ir/tac/tac'
require_relative '../ir/ssa/ssa'
require_relative '../ir/cfg'
require_relative '../ir/rcvm/vm'
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
      tac = TAC.to_tac(main)
      puts tac.tac_list
      puts 'To CFG'
      cfg = CFG.to_cfg(tac.tac_list)
      puts cfg
      cfg.to_dot("/home/homura/Code/Rc-lang/test.png")
      puts 'To roads'
      roads = CFG.search_all_branches(cfg)
      puts roads
      puts 'To VM Inst'
      vm_list = RCVM.to_vm_inst(tac)
      puts vm_list
      File.open('/home/homura/Code/RCVM/cmake-build-debug/inst.rcvi', 'w') do |f|
        f.write(vm_list.map(&:to_s).join("\n"))
      end
    end
  end
end