
module Rc
  module FFI
    def call(name, args, evaluator)
      # TODO:how to process args
      as = args.map{|arg| evaluator.evaluate(arg)}
      begin
        method(name.to_sym).call(*as)
      rescue NameError => e
        raise SymbolNotFoundError.new(name)
      end
    end

    module_function :call
  end
end