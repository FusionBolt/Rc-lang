# TODO:记录历史记录，上下键切换，抽出一个类？？
# TODO:内容需要换行怎么办，命令行工具？
# TODO:补全
# TODO:ctrl D退出
# TODO:save env
module Rc
  class REPL
    def initialize
      @history = []
      @interpreter = Interpreter.new
    end

    def run(prompt = 'Rc> ')
      $logger.level = Logger::ERROR
      while true
        print prompt
        input = gets
        @history << input
        if input.start_with? ':'
          instruction(input)
        else
          interpret(input)
        end
      end
    end

    def interpret(input)
      # TODO:changed
      begin
        val = @interpreter.run(input)
      rescue => e
        puts "error occurred:#{e}"
      end
      puts val[0].cur_stmt.inspect
    end

    def instruction(input)
      # TODO:implement
      # TODO:set prompt
      case input.chomp[1..-1]
      when 'env'
        p 'env'
      when 'help'
        help
      when 'history'
        p 'history'
      when 'q'
        # TODO:exit
      end
    end

    def help

    end
  end
end