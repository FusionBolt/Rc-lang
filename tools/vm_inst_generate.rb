require './ir/vm/generator'

File.open('../RCVM/include/instructions.hpp', 'w+').write(gen_inst_src)
