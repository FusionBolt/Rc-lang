require_relative './hack'

module Rc
  module Helper
    def under_score_class_name(obj)
      obj.demodulize_class.underscore
    end

    def reverse_kv(hash)
      sum = {}
      hash.each do |(k, vs)|
        vs.each do |v|
          sum[v] = sum.fetch(v, Set[]) + Set[k]
        end
      end
      sum
    end

    module_function :under_score_class_name
    module_function :reverse_kv
  end
end
