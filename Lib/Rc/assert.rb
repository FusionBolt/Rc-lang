require_relative '../error'

module Rc
  module Lib
    def assert(a, b)
      if a == b
        true
      else
        raise AssertFailedError.new(a, b)
      end
    end
  end
end