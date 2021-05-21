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

    def assert_true(a)
      if a != true
        raise AssertFailedError.new(a)
      end
    end
  end
end