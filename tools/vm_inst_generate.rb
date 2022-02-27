require './ir/vm/generator'

File.open('../RCVM/include/instructions.hpp', 'w+').write(gen_inst_src)
File.open('../RCVM/src/vm_accept.cpp', 'w+').write(gen_visitor)
