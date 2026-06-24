import express from 'express';
import { PrismaClient } from './generated/prisma/client.ts';
import 'dotenv/config';
import pg from 'pg';
import { PrismaPg } from '@prisma/adapter-pg';

const router = express.Router();

const pool = new pg.Pool({ 
  connectionString: process.env.DATABASE_URL,
  ssl: { rejectUnauthorized: false }
});
const adapter = new PrismaPg(pool);
const prisma = new PrismaClient({ adapter });

// Create Student
router.post('/students', async (req, res) => {
  try {
    const { name, roll_no, age, class: className } = req.body;
    const student = await prisma.student.create({
      data: { name, roll_no, age, class: className }
    });
    res.status(201).json(student);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

// Read all Students
router.get('/students', async (req, res) => {
  try {
    const students = await prisma.student.findMany();
    res.json(students);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Read single Student
router.get('/students/:id', async (req, res) => {
  try {
    const id = parseInt(req.params.id);
    const student = await prisma.student.findUnique({ where: { id } });
    if (student) {
      res.json(student);
    } else {
      res.status(404).json({ error: 'Student not found' });
    }
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Update Student
router.put('/students/:id', async (req, res) => {
  try {
    const id = parseInt(req.params.id);
    const { name, roll_no, age, class: className } = req.body;
    const student = await prisma.student.update({
      where: { id },
      data: { name, roll_no, age, class: className }
    });
    res.json(student);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

// Delete Student
router.delete('/students/:id', async (req, res) => {
  try {
    const id = parseInt(req.params.id);
    await prisma.student.delete({ where: { id } });
    res.json({ message: 'Student deleted successfully' });
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

export default router;
