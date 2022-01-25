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
      vm_list = VM.to_vm_inst(ast, global_env)
      puts vm_list
      root = '/home/homura/Code/RCVM/cmake-build-debug/'
      generate(root, 'inst.rcvi', vm_list.map(&:to_s).join("\n"))
      # generate(root, 'fun.rcsym', gen_sym_table(global_env))
      generate(root, 'class_table.rckls', gen_class_table(global_env))
    end

    def gen_class_table(global_env)
      # todo:instance_vars should not save var value
      global_env.class_table.map do |class_name, table|
        <<SRC
#{class_name}
#{table.instance_vars.keys.map(&:to_s).join(' ')}
#{table.instance_methods.map{|name, info| gen_sym_table(name, info)}.join("\n") }
SRC
      end.join("\n")
    end

    def gen_sym_table(name, method_info)
        "#{name} #{method_info.args.size} #{method_info.env.size} #{method_info.offset}"
    end

    def generate(root, name, content)
      File.open(File.join(root, name), 'w') do |f|
        f.write(content)
      end
    end

    # def gen_sym_table(global_env)
    #   global_env.define_env.map do |name, fun_table|
    #     "#{name} #{fun_table.args.size} #{fun_table.local_sym_table.size} #{fun_table.offset}"
    #   end.join("\n")
    # end

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