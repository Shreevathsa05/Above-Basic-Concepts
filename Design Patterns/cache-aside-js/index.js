import express from 'express';
import basiccrudRouter from './basiccrud.js';

const app = express();
app.use(express.json());

// Link with app.use
app.use('/api', basiccrudRouter);

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
