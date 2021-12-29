module Rc
  module Helper
    # todo: hack to obj
    def pure_class_name(obj)
      s = obj.class.to_s
      if s.include? '::'
        s.split('::')[-1]
      else
        s
      end
    end

    def under_score_class_name(obj)
      underscore pure_class_name obj
    end

    def underscore(str)
      str.gsub(/::/, '/').
        gsub(/([A-Z]+)([A-Z][a-z])/, '\1_\2').
        gsub(/([a-z\d])([A-Z])/, '\1_\2').
        tr("-", "_").
        downcase
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
    module_function :underscore
    module_function :reverse_kv
    module_function :pure_class_name
  end
end
