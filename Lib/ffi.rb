require './Lib/Rc/assert'

module Rc
  class FFI
    extend Rc::Lib

    def self.call(name, args, evaluator)
      # TODO:how to process args
      as = args.map { |arg| evaluator.evaluate(arg) }
      begin
        method(name.to_sym).call(*as)
      rescue NameError => e
        raise SymbolNotFoundError.new(name)
      end
    end
  end
end