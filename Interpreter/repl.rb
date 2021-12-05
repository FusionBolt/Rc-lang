# TODO:记录历史记录，上下键切换，抽出一个类？？
# TODO:内容需要换行怎么办，命令行工具？
# TODO:补全
# TODO:ctrl D退出
# TODO:save env

require './Lib/error'
require './Interpreter/ast_interpreter'

module Rc
  class REPLError < RcError

  end
  class InstructionNotExistError < REPLError
    def initialize(inst)
      @inst = inst
    end
    def inspect
      "Instruction #{@inst} Not Exist"
    end
  end

  class REPL
    def initialize
      @history = []
      @interpreter = ASTInterpreter.new
      @prompt = 'Rc> '
    end

    def run(prompt = @prompt)
      @prompt = prompt
      $logger.level = Logger::ERROR
      while true
        print @prompt
        input = gets
        begin
        if input.start_with? ':'
          instruction(input)
        else
          interpret(input)
        end
        # avoid display cur :history when run :history
        @history << input
        rescue REPLError => e
          puts e.inspect
        end
      end
    end

    def interpret(input)
      # TODO:changed
      @interpreter.run(input).each{|v| puts v}
    end

    def instruction(input)
      inst = input.chomp[1..]
      inst_list = inst.split(' ')
      if inst_list.length == 1
        single_inst(inst)
      else
        args_inst(inst_list)
      end
    end

    def args_inst(inst_list)
      args = inst_list[1..]
      case inst_list[0]
        # TODO:if prompt has space
      when 'prompt'
        @prompt = args[0]
      when 'touch'
        if args[0] == "fish"
          puts '你可别摸了'
        end
      else
        raise InstructionNotExistError.new(inst_list.join(' '))
      end
    end

    def single_inst(inst)
      case inst
      when 'env'
        puts @interpreter.env.inspect
      when 'help'
        help
      when 'history'
        puts @history
      when 'q'
        exit
      else
        raise InstructionNotExistError.new(inst)
      end
    end

    def help
      #TODO:finish
      puts "unfinished help"
    end
  end
end